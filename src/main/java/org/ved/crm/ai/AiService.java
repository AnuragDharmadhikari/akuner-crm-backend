package org.ved.crm.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.ai.dto.*;
import org.ved.crm.ai.provider.AiProvider;
import org.ved.crm.ai.provider.AiResponse;
import org.ved.crm.billing.InvoiceRepository;
import org.ved.crm.chemist.ChemistRepository;
import org.ved.crm.common.exception.ResourceNotFoundException;
import org.ved.crm.doctor.Doctor;
import org.ved.crm.doctor.DoctorRepository;
import org.ved.crm.order.OrderRepository;
import org.ved.crm.stockist.Stockist;
import org.ved.crm.stockist.StockistRepository;
import org.ved.crm.territory.Territory;
import org.ved.crm.territory.TerritoryRepository;
import org.ved.crm.user.UserRepository;
import org.ved.crm.visit.VisitRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiService {

    private final AiProvider aiProvider;
    private final AiInteractionLogger aiInteractionLogger;
    private final DoctorRepository doctorRepository;
    private final VisitRepository visitRepository;
    private final StockistRepository stockistRepository;
    private final TerritoryRepository territoryRepository;
    private final ChemistRepository chemistRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    // GPT-4o-mini pricing constants
    private static final BigDecimal INPUT_COST_PER_TOKEN = new BigDecimal("0.00000015");
    private static final BigDecimal OUTPUT_COST_PER_TOKEN = new BigDecimal("0.00000060");

    // Bilingual instruction appended to every system prompt
    // This single instruction makes every feature bilingual automatically
    // AI is instructed to use [EN] and [MR] tags for every text field
    // We parse these tags in the helper methods below
    private static final String BILINGUAL_INSTRUCTION = """
            
            IMPORTANT LANGUAGE REQUIREMENT:
            For every text field in your response, you MUST provide both:
            [EN]: English text here
            [MR]: मराठी मजकूर येथे
            
            Example format:
            ANALYSIS:
            [EN]: The doctor shows high engagement with 8 completed visits.
            [MR]: डॉक्टरांनी ८ पूर्ण भेटींसह उच्च सहभाग दर्शवला आहे.
            
            Every ANALYSIS, RECOMMENDATIONS, RISK_ANALYSIS, RECOMMENDED_ACTION,
            NARRATIVE, STRENGTHS, CONCERNS, PRODUCT_FOCUS, TALKING_POINTS,
            ACTIVE_SCHEMES, VISIT_STRATEGY, LAST_VISIT_SUMMARY,
            RECOMMENDED_PRODUCTS, REASONING, APPLICABLE_SCHEMES,
            FOLLOW_UP_MESSAGE field must have both [EN] and [MR] versions.
            Numeric fields (SCORE, RISK_SCORE) remain as plain numbers.
            LEVEL and RISK_LEVEL remain as plain English words.
            """;

    // ── Feature 1: Doctor Engagement Score ────────────────────────────────────
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'REP')")
    @Cacheable(value = "ai::doctor-engagement", key = "#doctorId")
    public DoctorEngagementDto getDoctorEngagement(UUID doctorId){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(()->new ResourceNotFoundException("Doctor","id",doctorId));

        var visits = visitRepository.findByDoctorIdWithDetails(doctorId);

        StringBuilder visitHistory = new StringBuilder();
        if (visits.isEmpty()){
            visitHistory.append("No visits recorded yet.");
        }else {
            visits.stream().limit(5).forEach(v->{
                visitHistory.append("- Visit on ").append(v.getVisitDate())
                        .append(", Status: ").append(v.getStatus())
                        .append(", Notes: ").append(
                                v.getNotes() != null ? v.getNotes() : "none"
                        ).append("\n");
            });
        }

        // System prompt defines AI persona + output format + bilingual requirement
        String systemPrompt = """
                You are an expert pharmaceutical sales analyst with 15 years
                of experience in the Indian pharma market. You analyse doctor
                engagement data for medical sales representatives.
                
                Your response must follow this EXACT format:
                SCORE: [number 0-100]
                LEVEL: [exactly one of: HIGH, MEDIUM, LOW]
                ANALYSIS:
                [EN]: [2-3 sentence analysis in English]
                [MR]: [same analysis in Marathi]
                RECOMMENDATIONS:
                [EN]: [3 recommendations numbered 1, 2, 3 in English]
                [MR]: [same 3 recommendations in Marathi]
                
                HIGH = 75-100, MEDIUM = 40-74, LOW = 0-39.
                Reference actual numbers from the data provided.
                """
                + BILINGUAL_INSTRUCTION;

        String userPrompt = String.format("""
                Analyse engagement for this doctor:
                
                DOCTOR PROFILE:
                Name: %s
                Specialty: %s
                Tier: %s
                Territory: %s
                Active: %s
                
                VISIT HISTORY (last 5):
                %s
                Total visits: %d
                Completed visits: %d
                """,
                doctor.getFullName(),
                doctor.getSpecialty(),
                doctor.getTier(),
                doctor.getTerritory() != null ?
                        doctor.getTerritory().getName() : "Unassigned",
                doctor.isActive() ? "Yes" : "No",
                visitHistory.toString(),
                visits.size(),
                visits.stream().filter(v ->
                        v.getStatus().name().equals("COMPLETED")).count()
        );

        long startTime = System.currentTimeMillis();
        AiResponse aiResponse = aiProvider.complete(systemPrompt, userPrompt);
        long durationMs = System.currentTimeMillis() - startTime;

        String content = aiResponse.content();

        aiInteractionLogger.log("DOCTOR_ENGAGEMENT", doctorId, aiResponse, durationMs,getCurrentUserId());

        return new DoctorEngagementDto(
                doctorId.toString(),
                doctor.getFullName(),
                parseIntField(content, "SCORE:", 50),
                parseTextField(content, "LEVEL:", "MEDIUM"),
                parseEnglish(content, "ANALYSIS:"),
                parseMarathi(content, "ANALYSIS:"),
                parseEnglish(content, "RECOMMENDATIONS:"),
                parseMarathi(content, "RECOMMENDATIONS:")
        );
    }

    // ── Feature 2: Visit Briefing ──────────────────────────────────────────────
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'REP')")
    @Cacheable(value = "ai::visit-briefing", key = "#visitId")
    public VisitBriefingDto getVisitBriefing(UUID visitId) {
        var visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResourceNotFoundException("Visit","id",visitId));

        Doctor doctor = visit.getDoctor();

        var previousVisits = visitRepository.findByDoctorIdWithDetails(doctor.getId());

        StringBuilder previousVisitsSummary = new StringBuilder();
        previousVisits.stream().limit(3).forEach(v -> {
            previousVisitsSummary.append("- ")
                    .append(v.getVisitDate())
                    .append(": Status=").append(v.getStatus())
                    .append(", Notes=").append(
                            v.getNotes() != null ? v.getNotes() : "none")
                    .append("\n");
        });

        String systemPrompt = """
                You are an expert pharmaceutical sales coach preparing a medical
                representative for a doctor visit in India.
                
                Your response must follow this EXACT format:
                LAST_VISIT_SUMMARY:
                [EN]: [1-2 sentences in English]
                [MR]: [same in Marathi]
                PRODUCT_FOCUS:
                [EN]: [products to detail and why in English]
                [MR]: [same in Marathi]
                TALKING_POINTS:
                [EN]: [3 talking points in English]
                [MR]: [same in Marathi]
                ACTIVE_SCHEMES:
                [EN]: [schemes or "No active schemes" in English]
                [MR]: [same in Marathi]
                VISIT_STRATEGY:
                [EN]: [one strategic sentence in English]
                [MR]: [same in Marathi]
                """
                + BILINGUAL_INSTRUCTION;

        String userPrompt = String.format("""
                Prepare pre-visit briefing:
                
                VISIT: Date=%s, Status=%s
                DOCTOR: Name=%s, Specialty=%s, Tier=%s
                
                PREVIOUS VISITS:
                %s
                """,
                visit.getVisitDate(),
                visit.getStatus(),
                doctor.getFullName(),
                doctor.getSpecialty(),
                doctor.getTier(),
                previousVisitsSummary.toString().isEmpty() ?
                        "No previous visits." : previousVisitsSummary.toString()
        );

        long startTime = System.currentTimeMillis();
        AiResponse aiResponse = aiProvider.complete(systemPrompt, userPrompt);
        long durationMs = System.currentTimeMillis() - startTime;

        String content = aiResponse.content();

        aiInteractionLogger.log("VISIT_BRIEFING", visitId, aiResponse, durationMs,getCurrentUserId());

        return new VisitBriefingDto(
                visitId.toString(),
                doctor.getFullName(),
                parseEnglish(content, "LAST_VISIT_SUMMARY:"),
                parseMarathi(content, "LAST_VISIT_SUMMARY:"),
                parseEnglish(content, "PRODUCT_FOCUS:"),
                parseMarathi(content, "PRODUCT_FOCUS:"),
                parseEnglish(content, "TALKING_POINTS:"),
                parseMarathi(content, "TALKING_POINTS:"),
                parseEnglish(content, "ACTIVE_SCHEMES:"),
                parseMarathi(content, "ACTIVE_SCHEMES:"),
                parseEnglish(content, "VISIT_STRATEGY:"),
                parseMarathi(content, "VISIT_STRATEGY:")
        );

    }

    // ── Feature 3: Stockist Payment Risk ──────────────────────────────────────
    @PreAuthorize("hasRole('OWNER')")
    @Cacheable(value = "ai::payment-risk", key = "#stockistId")
    public PaymentRiskDto getStockistPaymentRisk(UUID stockistId) {

        Stockist stockist = stockistRepository.findByIdWithDetails(stockistId)
                .orElseThrow(()->new ResourceNotFoundException("Stockist","id",stockistId));

        var outstandingInvoices = invoiceRepository
                .findOutstandingInvoices()
                .stream()
                .filter(i -> i.getBilledToName() != null &&
                        i.getBilledToName().equals(stockist.getFirmName()))
                .toList();

        BigDecimal totalOutstanding = outstandingInvoices.stream()
                .map(i -> i.getOutstandingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder invoiceSummary = new StringBuilder();
        if (outstandingInvoices.isEmpty()) {
            invoiceSummary.append("No outstanding invoices.");
        } else {
            outstandingInvoices.forEach(i ->
                    invoiceSummary.append("- Invoice: ")
                            .append(i.getInvoiceNumber())
                            .append(", Outstanding: ₹")
                            .append(i.getOutstandingAmount())
                            .append(", Days since issued: ")
                            .append(i.getDaysSinceIssued())
                            .append("\n")
            );
        }

        String systemPrompt = """
                You are a credit risk analyst for an Indian pharmaceutical company
                assessing payment risk of stockists (pharmaceutical distributors).
                
                Your response must follow this EXACT format:
                RISK_LEVEL: [exactly one of: LOW, MEDIUM, HIGH]
                RISK_SCORE: [number 0-100]
                AVERAGE_PAYMENT_DAYS: [estimate like "30-45 days"]
                RISK_ANALYSIS:
                [EN]: [2-3 sentence risk assessment in English]
                [MR]: [same in Marathi]
                RECOMMENDED_ACTION:
                [EN]: [one specific action in English]
                [MR]: [same in Marathi]
                
                LOW=paying on time, MEDIUM=some delays, HIGH=consistent delays 45+ days
                """
                + BILINGUAL_INSTRUCTION;

        String userPrompt = String.format("""
                Assess payment risk:
                
                STOCKIST: %s, Owner: %s, State: %s, City: %s
                
                OUTSTANDING INVOICES:
                %s
                Total Outstanding: ₹%s
                Count: %d
                """,
                stockist.getFirmName(),
                stockist.getOwnerName(),
                stockist.getState(),
                stockist.getCity(),
                invoiceSummary.toString(),
                totalOutstanding.toPlainString(),
                outstandingInvoices.size()
        );

        long startTime = System.currentTimeMillis();
        AiResponse aiResponse = aiProvider.complete(systemPrompt, userPrompt);
        long durationMs = System.currentTimeMillis() - startTime;

        String content = aiResponse.content();

        aiInteractionLogger.log("PAYMENT_RISK", stockistId, aiResponse, durationMs,getCurrentUserId());

        return new PaymentRiskDto(
                stockistId.toString(),
                stockist.getFirmName(),
                parseTextField(content, "RISK_LEVEL:", "MEDIUM"),
                parseIntField(content, "RISK_SCORE:", 50),
                totalOutstanding,
                parseTextField(content, "AVERAGE_PAYMENT_DAYS:", "Unknown"),
                parseEnglish(content, "RISK_ANALYSIS:"),
                parseMarathi(content, "RISK_ANALYSIS:"),
                parseEnglish(content, "RECOMMENDED_ACTION:"),
                parseMarathi(content, "RECOMMENDED_ACTION:")
        );

    }

    // ── Feature 4: Chemist Payment Risk ───────────────────────────────────────
// Cached 6h per chemist
// OWNER only — financial risk data is sensitive
    @PreAuthorize("hasRole('OWNER')")
    @Cacheable(value = "ai::chemist-payment-risk", key = "#chemistId")
    public PaymentRiskDto getChemistPaymentRisk(UUID chemistId) {

        var chemist = chemistRepository.findById(chemistId)
                .orElseThrow(() -> new ResourceNotFoundException("Chemist","id",chemistId));

        // Filter outstanding invoices billed to this chemist
        var outstandingInvoices = invoiceRepository
                .findOutstandingInvoices()
                .stream()
                .filter(i -> i.getBilledToName() != null &&
                        i.getBilledToName().equals(chemist.getFirmName()))
                .toList();

        BigDecimal totalOutstanding = outstandingInvoices.stream()
                .map(i -> i.getOutstandingAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder invoiceSummary = new StringBuilder();
        if (outstandingInvoices.isEmpty()) {
            invoiceSummary.append("No outstanding invoices.");
        } else {
            outstandingInvoices.forEach(i ->
                    invoiceSummary.append("- Invoice: ")
                            .append(i.getInvoiceNumber())
                            .append(", Outstanding: ₹")
                            .append(i.getOutstandingAmount())
                            .append(", Days since issued: ")
                            .append(i.getDaysSinceIssued())
                            .append("\n")
            );
        }

        // Same prompt structure as stockist risk
        // Context differs — chemist is retail pharmacy, smaller volumes
        // AI understands this distinction from the role description
        String systemPrompt = """
            You are a credit risk analyst for an Indian pharmaceutical company
            assessing payment risk of chemists (retail pharmacies).
            
            Your response must follow this EXACT format:
            RISK_LEVEL: [exactly one of: LOW, MEDIUM, HIGH]
            RISK_SCORE: [number 0-100]
            AVERAGE_PAYMENT_DAYS: [estimate like "30-45 days"]
            RISK_ANALYSIS:
            [EN]: [2-3 sentence risk assessment in English]
            [MR]: [same in Marathi]
            RECOMMENDED_ACTION:
            [EN]: [one specific action in English]
            [MR]: [same in Marathi]
            
            LOW=paying on time, MEDIUM=some delays, HIGH=consistent delays 45+ days
            Chemists are retail pharmacies — typically smaller order volumes than stockists.
            """
                + BILINGUAL_INSTRUCTION;

        String userPrompt = String.format("""
            Assess payment risk for this chemist:
            
            CHEMIST: %s, Owner: %s, State: %s, City: %s
            Drug License: %s
            
            OUTSTANDING INVOICES:
            %s
            Total Outstanding: ₹%s
            Count: %d
            """,
                chemist.getFirmName(),
                chemist.getOwnerName(),
                chemist.getState(),
                chemist.getCity(),
                chemist.getDrugLicenseNumber(),
                invoiceSummary.toString(),
                totalOutstanding.toPlainString(),
                outstandingInvoices.size()
        );

        long startTime = System.currentTimeMillis();
        AiResponse aiResponse = aiProvider.complete(systemPrompt, userPrompt);
        long durationMs = System.currentTimeMillis() - startTime;

        String content = aiResponse.content();

        aiInteractionLogger.log("CHEMIST_PAYMENT_RISK", chemistId, aiResponse, durationMs,getCurrentUserId());

        return new PaymentRiskDto(
                chemistId.toString(),
                chemist.getFirmName(),
                parseTextField(content, "RISK_LEVEL:", "MEDIUM"),
                parseIntField(content, "RISK_SCORE:", 50),
                totalOutstanding,
                parseTextField(content, "AVERAGE_PAYMENT_DAYS:", "Unknown"),
                parseEnglish(content, "RISK_ANALYSIS:"),
                parseMarathi(content, "RISK_ANALYSIS:"),
                parseEnglish(content, "RECOMMENDED_ACTION:"),
                parseMarathi(content, "RECOMMENDED_ACTION:")
        );
    }

    // ── Feature 5: Territory Narrative ────────────────────────────────────────
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @Cacheable(value = "ai::territory-narrative", key = "#territoryId")
    public TerritoryNarrativeDto getTerritoryNarrative(UUID territoryId) {

        Territory territory = territoryRepository.findById(territoryId)
                .orElseThrow(()->new ResourceNotFoundException("Territory","id",territoryId));

        var doctors = doctorRepository.findByTerritoryId(territoryId);

        long totalVisits = doctors.stream()
                .mapToLong(d -> visitRepository
                        .findByDoctorIdWithDetails(d.getId()).size())
                .sum();

        long completedVisits = doctors.stream()
                .mapToLong(d -> visitRepository.findByDoctorIdWithDetails(d.getId())
                        .stream()
                        .filter(v -> v.getStatus().name().equals("COMPLETED"))
                        .count())
                .sum();

        String systemPrompt = """
                You are a senior business analyst writing executive territory
                performance reports for an Indian pharmaceutical company.
                
                Your response must follow this EXACT format:
                NARRATIVE:
                [EN]: [200-word executive narrative in English]
                [MR]: [same in Marathi]
                STRENGTHS:
                [EN]: [3 strengths numbered 1,2,3 in English]
                [MR]: [same in Marathi]
                CONCERNS:
                [EN]: [3 concerns numbered 1,2,3 in English]
                [MR]: [same in Marathi]
                RECOMMENDATIONS:
                [EN]: [3 recommendations numbered 1,2,3 in English]
                [MR]: [same in Marathi]
                """
                + BILINGUAL_INSTRUCTION;

        String userPrompt = String.format("""
                Generate territory performance narrative:
                
                Territory: %s, State: %s, Zone: %s
                Total Doctors: %d
                Total Visits: %d, Completed: %d
                Completion Rate: %s%%
                Tier A: %d, Tier B: %d, Tier C: %d doctors
                """,
                territory.getName(),
                territory.getState(),
                territory.getZone(),
                doctors.size(),
                totalVisits,
                completedVisits,
                totalVisits > 0 ?
                        String.valueOf(completedVisits * 100 / totalVisits) : "0",
                doctors.stream().filter(d ->
                        d.getTier().name().equals("A")).count(),
                doctors.stream().filter(d ->
                        d.getTier().name().equals("B")).count(),
                doctors.stream().filter(d ->
                        d.getTier().name().equals("C")).count()
        );

        long startTime = System.currentTimeMillis();
        AiResponse aiResponse = aiProvider.complete(systemPrompt, userPrompt);
        long durationMs = System.currentTimeMillis() - startTime;

        String content = aiResponse.content();
        String period = java.time.LocalDate.now()
                .getMonth().getDisplayName(
                        java.time.format.TextStyle.FULL,
                        java.util.Locale.ENGLISH)
                + " " + java.time.LocalDate.now().getYear();

        aiInteractionLogger.log("TERRITORY_NARRATIVE", territoryId, aiResponse, durationMs,getCurrentUserId());

        return new TerritoryNarrativeDto(
                territoryId.toString(),
                territory.getName(),
                period,
                parseEnglish(content, "NARRATIVE:"),
                parseMarathi(content, "NARRATIVE:"),
                parseEnglish(content, "STRENGTHS:"),
                parseMarathi(content, "STRENGTHS:"),
                parseEnglish(content, "CONCERNS:"),
                parseMarathi(content, "CONCERNS:"),
                parseEnglish(content, "RECOMMENDATIONS:"),
                parseMarathi(content, "RECOMMENDATIONS:")
        );

    }

    // ── Feature 6: Smart Order Recommendation ─────────────────────────────────

    @PreAuthorize("hasAnyRole('OWNER', 'REP')")
    @Cacheable(value = "ai::order-recommendation", key = "#chemistId")
    public OrderRecommendationDto getOrderRecommendation(UUID chemistId) {
        var chemist = chemistRepository.findById(chemistId)
                .orElseThrow(()->new ResourceNotFoundException("Chemist","id",chemistId));

        var recentOrders = orderRepository.findByChemistIdWithDetails(chemistId);

        StringBuilder orderHistory = new StringBuilder();
        if (recentOrders.isEmpty()) {
            orderHistory.append("No previous orders.");
        } else {
            recentOrders.stream().limit(3).forEach(o ->
                    orderHistory.append("- Order: ").append(o.getId())
                            .append(", Type: ").append(o.getFulfillmentType())
                            .append(", Status: ").append(o.getStatus())
                            .append("\n")
            );
        }

        String systemPrompt = """
                You are a pharmaceutical sales optimization expert helping
                medical representatives place smart orders for chemists in India.
                
                Your response must follow this EXACT format:
                RECOMMENDED_PRODUCTS:
                [EN]: [3 products with quantities and reasons in English]
                [MR]: [same in Marathi]
                REASONING:
                [EN]: [2-3 sentence reasoning in English]
                [MR]: [same in Marathi]
                APPLICABLE_SCHEMES:
                [EN]: [schemes or "No active schemes" in English]
                [MR]: [same in Marathi]
                ESTIMATED_ORDER_VALUE: [value like "₹2,500 - ₹3,000"]
                """
                + BILINGUAL_INSTRUCTION;

        String userPrompt = String.format("""
                Generate order recommendation:
                
                CHEMIST: %s, Owner: %s, State: %s, City: %s
                Drug License: %s
                
                RECENT ORDERS:
                %s
                Total Previous Orders: %d
                """,
                chemist.getFirmName(),
                chemist.getOwnerName(),
                chemist.getState(),
                chemist.getCity(),
                chemist.getDrugLicenseNumber(),
                orderHistory.toString(),
                recentOrders.size()
        );

        long startTime = System.currentTimeMillis();
        AiResponse aiResponse = aiProvider.complete(systemPrompt, userPrompt);
        long durationMs = System.currentTimeMillis() - startTime;

        String content = aiResponse.content();

        aiInteractionLogger.log("ORDER_RECOMMENDATION", chemistId, aiResponse, durationMs,getCurrentUserId());

        return new OrderRecommendationDto(
                chemistId.toString(),
                chemist.getFirmName(),
                parseEnglish(content, "RECOMMENDED_PRODUCTS:"),
                parseMarathi(content, "RECOMMENDED_PRODUCTS:"),
                parseEnglish(content, "REASONING:"),
                parseMarathi(content, "REASONING:"),
                parseEnglish(content, "APPLICABLE_SCHEMES:"),
                parseMarathi(content, "APPLICABLE_SCHEMES:"),
                parseTextField(content, "ESTIMATED_ORDER_VALUE:", "Unknown")
        );

    }

    // ── Feature 7: Payment Follow-up Message ──────────────────────────────────
    @PreAuthorize("hasRole('OWNER')")
    public PaymentFollowUpDto getPaymentFollowUp(UUID invoiceId) {
        var outstandingInvoices = invoiceRepository.findOutstandingInvoices();
        var invoiceData = outstandingInvoices.stream()
                .filter(i -> i.getInvoiceId().equals(invoiceId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Invoice","id", invoiceId));

        long daysOverdue = invoiceData.getDaysSinceIssued();
        String tone = daysOverdue > 30 ? "URGENT" :
                daysOverdue > 14 ? "FIRM" : "GENTLE";

        String systemPrompt = String.format("""
                You are a professional accounts receivable manager for an Indian
                pharmaceutical company writing payment follow-up messages.
                
                Tone required: %s
                GENTLE = polite reminder, assume good faith
                FIRM = clear expectation of immediate payment
                URGENT = serious tone, mention consequences
                
                Your response must follow this EXACT format:
                FOLLOW_UP_MESSAGE:
                [EN]: [complete professional message in English, 100-150 words,
                include invoice number, amount, days overdue]
                [MR]: [same message in Marathi]
                
                Use Indian business communication style.
                Address recipient professionally.
                Company name is "VedPharm".
                """, tone)
                + BILINGUAL_INSTRUCTION;

        String userPrompt = String.format("""
                Generate payment follow-up:
                
                Invoice: %s
                Billed To: %s
                Original Amount: ₹%s
                Amount Paid: ₹%s
                Outstanding: ₹%s
                Days Since Issued: %d
                Status: %s
                """,
                invoiceData.getInvoiceNumber(),
                invoiceData.getBilledToName(),
                invoiceData.getGrandTotal().toPlainString(),
                invoiceData.getTotalPaid().toPlainString(),
                invoiceData.getOutstandingAmount().toPlainString(),
                daysOverdue,
                invoiceData.getStatus()
        );

        long startTime = System.currentTimeMillis();
        AiResponse aiResponse = aiProvider.complete(systemPrompt, userPrompt);
        long durationMs = System.currentTimeMillis() - startTime;

        String content = aiResponse.content();

        aiInteractionLogger.log("PAYMENT_FOLLOW_UP", invoiceId, aiResponse, durationMs,getCurrentUserId());

        return new PaymentFollowUpDto(
                invoiceId.toString(),
                invoiceData.getInvoiceNumber(),
                invoiceData.getBilledToName(),
                daysOverdue,
                invoiceData.getOutstandingAmount(),
                tone,
                parseEnglish(content, "FOLLOW_UP_MESSAGE:"),
                parseMarathi(content, "FOLLOW_UP_MESSAGE:")
        );
    }

        // ── Private Helper Methods ─────────────────────────────────────────────────

    private UUID getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder
                    .getContext().getAuthentication();
            assert auth != null;
            return userRepository.findByEmail(auth.getName())
                    .orElseThrow()
                    .getId();
        } catch (Exception e) {
            return UUID.fromString("00000000-0000-0000-0000-000000000000");
        }
    }

    // Extracts the [EN] portion of a bilingual field
    // Looks for [EN]: tag after the field label
    private String parseEnglish(String content, String label) {
        return parseBilingualTag(content, label, "[EN]:",
                "No English content available.");
    }

    // Extracts the [MR] portion of a bilingual field
    // Looks for [MR]: tag after the field label
    private String parseMarathi(String content, String label) {
        return parseBilingualTag(content, label, "[MR]:",
                "मराठी मजकूर उपलब्ध नाही.");
    }

    // Core bilingual parser
    // Finds the field label, then finds the language tag within that field
    // Returns text between the language tag and the next tag or field label
    private String parseBilingualTag(String content, String fieldLabel,
                                     String langTag, String defaultValue) {
        try {
            // Find where the field starts
            int fieldIndex = content.indexOf(fieldLabel);
            if (fieldIndex == -1) return defaultValue;

            // Get everything after the field label
            String afterField = content.substring(
                    fieldIndex + fieldLabel.length());

            // Find the language tag within this field's content
            int tagIndex = afterField.indexOf(langTag);
            if (tagIndex == -1) return defaultValue;

            // Get everything after the language tag
            String afterTag = afterField.substring(
                    tagIndex + langTag.length()).trim();

            // Find where this language section ends
            // It ends at the next [EN]: or [MR]: or a new FIELD_LABEL:
            int endIndex = afterTag.length();

            // Check for next language tag
            String otherTag = langTag.equals("[EN]:") ? "[MR]:" : "[EN]:";
            int otherTagIndex = afterTag.indexOf(otherTag);
            if (otherTagIndex != -1) {
                endIndex = Math.min(endIndex, otherTagIndex);
            }

            // Check for next field label (uppercase word followed by colon)
            // We look for patterns like "ANALYSIS:", "SCORE:", etc.
            for (String nextField : new String[]{
                    "SCORE:", "LEVEL:", "ANALYSIS:", "RECOMMENDATIONS:",
                    "LAST_VISIT_SUMMARY:", "PRODUCT_FOCUS:", "TALKING_POINTS:",
                    "ACTIVE_SCHEMES:", "VISIT_STRATEGY:", "RISK_LEVEL:",
                    "RISK_SCORE:", "AVERAGE_PAYMENT_DAYS:", "RISK_ANALYSIS:",
                    "RECOMMENDED_ACTION:", "NARRATIVE:", "STRENGTHS:",
                    "CONCERNS:", "RECOMMENDED_PRODUCTS:", "REASONING:",
                    "APPLICABLE_SCHEMES:", "ESTIMATED_ORDER_VALUE:",
                    "FOLLOW_UP_MESSAGE:"}) {
                int idx = afterTag.indexOf(nextField);
                if (idx != -1 && idx < endIndex) {
                    endIndex = idx;
                }
            }

            String result = afterTag.substring(0, endIndex).trim();
            return result.isEmpty() ? defaultValue : result;

        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Parses plain text fields like LEVEL, RISK_LEVEL, ESTIMATED_ORDER_VALUE
    // These fields have no bilingual tags — just plain text after the label
    private String parseTextField(String content, String label,
                                  String defaultValue) {
        try {
            int labelIndex = content.indexOf(label);
            if (labelIndex == -1) return defaultValue;

            String afterLabel = content.substring(
                    labelIndex + label.length()).trim();

            // Take only the first line — these are single-line fields
            int newlineIndex = afterLabel.indexOf("\n");
            String value = newlineIndex == -1
                    ? afterLabel
                    : afterLabel.substring(0, newlineIndex).trim();

            return value.isEmpty() ? defaultValue : value;

        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Parses integer fields like SCORE and RISK_SCORE
    private int parseIntField(String content, String label, int defaultValue) {
        try {
            String text = parseTextField(content, label,
                    String.valueOf(defaultValue));
            String digits = text.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return defaultValue;
            int value = Integer.parseInt(
                    digits.substring(0, Math.min(digits.length(), 3)));
            return Math.max(0, Math.min(100, value));
        } catch (Exception e) {
            return defaultValue;
        }

    }
}
