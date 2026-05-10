package org.ved.crm.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.ai.AiInteractionRepository;
import org.ved.crm.billing.InvoiceRepository;
import org.ved.crm.inventory.BatchRepository;
import org.ved.crm.order.OrderRepository;
import org.ved.crm.returns.CreditNoteRepository;
import org.ved.crm.returns.ReturnRepository;
import org.ved.crm.target.CallTargetRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final InvoiceRepository invoiceRepository;
    private final CreditNoteRepository creditNoteRepository;
    private final ReturnRepository returnRepository;
    private final OrderRepository orderRepository;
    private final BatchRepository batchRepository;
    private final CallTargetRepository callTargetRepository;
    private final AiInteractionRepository aiInteractionRepository;

    // ── Revenue Summary
    // Cached in Redis for 1 hour — heavy GROUP BY across invoices table
    // unless condition — don't cache empty results
    // OWNER only — revenue is sensitive financial data

    @PreAuthorize("hasRole('OWNER')")
    @Cacheable(value = "analytics::revenue", unless = "#result.isEmpty()")
    public List<RevenueSummaryDto> getRevenueSummary() {
        return invoiceRepository.findRevenueSummary()
                .stream()
                .map(
                        p -> new RevenueSummaryDto(
                                p.getMonth(),
                                p.getTotalRevenue(),
                                p.getInvoiceCount(),
                                p.getAverageInvoiceValue()
                        )
                ).toList();
    }

    // ── GST Liability
    // Cached in Redis for 1 hour — JOIN across invoices + invoice_line_items
    // OWNER only — tax liability is confidential financial data

    @PreAuthorize("hasRole('OWNER')")
    @Cacheable(value = "analytics::gst", unless = "#result.isEmpty()")
    public List<GstLiabilityDto> getGstLiability() {
        return invoiceRepository.findGstLiability()
                .stream()
                .map(p-> new GstLiabilityDto(
                        p.getMonth(),
                        p.getTotalCgst(),
                        p.getTotalSgst(),
                        p.getTotalIgst(),
                        p.getTotalTaxLiability()
                ))
                .toList();
    }

    // ── Outstanding Invoices ───────────────────────────────────────────────────
    // Cached in Redis for 1 hour — complex 4-table JOIN with HAVING clause
    // OWNER and MANAGER — both need to track receivables
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @Cacheable(value = "analytics::outstanding", unless = "#result.isEmpty()")
    public List<OutstandingInvoiceDto> getOutstandingInvoices() {
        return invoiceRepository.findOutstandingInvoices()
                .stream()
                .map(p-> new OutstandingInvoiceDto(
                        p.getInvoiceId(),
                        p.getInvoiceNumber(),
                        p.getBilledToName(),
                        p.getGrandTotal(),
                        p.getTotalPaid(),
                        p.getTotalCreditApplied(),
                        p.getOutstandingAmount(),
                        p.getStatus(),
                        p.getDaysSinceIssued()
                ))
                .toList();
    }

    // ── Open Credit Note Total ─────────────────────────────────────────────────
    // Not cached — single-row scalar, extremely fast query
    // OWNER only — credit liability is sensitive
    @PreAuthorize("hasRole('OWNER')")
    public OpenCreditNoteTotalDto getOpenCreditNoteTotal() {
        // Single projection object — not a list
        // Repository method returns one row always (scalar aggregate)
        CreditNoteProjections.OpenCreditNoteTotalProjection p = creditNoteRepository.findOpenCreditNoteTotal();
        return new OpenCreditNoteTotalDto(
                p.getTotalOpenValue(),
                p.getOpenCount(),
                p.getStockistOpenValue(),
                p.getChemistOpenValue()
        );
    }

    // ── Top Stockists ──────────────────────────────────────────────────────────
    // Not cached — fast indexed query with LIMIT
    // :limit default is 10 — controller passes this value
    // OWNER only — revenue breakdown by partner is sensitive
    @PreAuthorize("hasRole('OWNER')")
    public List<TopPerformerDto> getTopStockists(int limit) {

        return invoiceRepository.findTopStockists(limit)
                .stream()
                .map(p-> new TopPerformerDto(
                        p.getId(),
                        p.getName(),
                        p.getState(),
                        p.getTotalRevenue(),
                        p.getInvoiceCount()
                ))
                .toList();
    }

    // ── Top Chemists ───────────────────────────────────────────────────────────
    // Mirror of getTopStockists — same DTO, different repository query

    @PreAuthorize("hasRole('OWNER')")
    public List<TopPerformerDto> getTopChemists(int limit) {

        return invoiceRepository.findTopChemists(limit)
                .stream()
                .map(p -> new TopPerformerDto(
                        p.getId(),
                        p.getName(),
                        p.getState(),
                        p.getTotalRevenue(),
                        p.getInvoiceCount()
                ))
                .toList();
    }

    // ── Rep Performance ────────────────────────────────────────────────────────
    // Cached in Redis for 1 hour — 5-table JOIN, most expensive query
    // OWNER and MANAGER — both need visibility into rep performance

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    @Cacheable(value = "analytics::reps", unless = "#result.isEmpty()")
    public List<RepPerformanceDto> getRepPerformance() {

        return orderRepository.findRepPerformance()
                .stream()
                .map(p->{
                    // Compute achievementPct safely in Java — not in SQL
                    // Guard: if targetVisits is null or zero, return null
                    // A null achievement is better than a divide-by-zero crash
                    BigDecimal achievementPct = null;
                    if (p.getTargetVisits() != null && p.getTargetVisits() > 0) {
                        achievementPct = BigDecimal
                                // Convert actualVisits to BigDecimal for division
                                .valueOf(p.getActualVisits() != null ? p.getActualVisits() : 0)
                                // Multiply by 100 first — then divide — avoids precision loss
                                .multiply(BigDecimal.valueOf(100))
                                .divide(
                                        BigDecimal.valueOf(p.getTargetVisits()),
                                        2,              // scale — 2 decimal places
                                        RoundingMode.HALF_UP
                                );
                    }

                    return new RepPerformanceDto(
                            p.getRepId(),
                            p.getRepName(),
                            p.getTotalVisits(),
                            p.getCompletedVisits(),
                            p.getTotalOrders(),
                            p.getTotalRevenue(),
                            p.getTargetVisits(),
                            achievementPct
                    );
                })
                .toList();

    }

    // ── Product Velocity ───────────────────────────────────────────────────────
    // Not cached — relatively fast indexed query on invoice_line_items
    // OWNER and MANAGER — product performance data
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public List<ProductVelocityDto> getProductVelocity() {
        // ProductVelocity query lives on InvoiceRepository because
        // invoice_line_items is the source of truth for units sold
        // We need to add this query — coming in next step
        return invoiceRepository.findProductVelocity()
                .stream()
                .map(p -> new ProductVelocityDto(
                        p.getProductId(),
                        p.getProductName(),
                        p.getMolecule(),
                        p.getHsnCode(),
                        p.getTotalUnitsSold(),
                        p.getTotalFreeUnits(),
                        p.getTotalUnitsDeducted(),
                        p.getTotalRevenue()
                ))
                .toList();
    }

    // ── Inventory Value ────────────────────────────────────────────────────────
    // Not cached — real-time stock value should reflect current state
    // Caching this could show stale inventory numbers after a sale
    // OWNER only — balance sheet data
    @PreAuthorize("hasRole('OWNER')")
    public List<InventoryValueDto> getInventoryValue() {

        return batchRepository.findInventoryValue()
                .stream()
                .map(p -> new InventoryValueDto(
                        p.getProductId(),
                        p.getProductName(),
                        p.getHsnCode(),
                        p.getDealerPrice(),
                        p.getTotalCurrentUnits(),
                        p.getTotalInventoryValue()
                ))
                .toList();
    }

    // ── Near Expiry Value ──────────────────────────────────────────────────────
    // Not cached — expiry risk is time-sensitive, cache would be misleading
    // A batch that was 91 days away yesterday is 90 days away today
    // OWNER only — risk management data
    @PreAuthorize("hasRole('OWNER')")
    public List<NearExpiryValueDto> getNearExpiryValue() {

        return batchRepository.findNearExpiryBatches()
                .stream()
                .map(p -> new NearExpiryValueDto(
                        p.getBatchId(),
                        p.getBatchNumber(),
                        p.getProductId(),
                        p.getProductName(),
                        p.getExpiryDate(),
                        p.getDaysUntilExpiry(),
                        p.getCurrentQuantity(),
                        p.getDealerPrice(),
                        p.getValueAtRisk()
                ))
                .toList();
    }

    // ── Target Achievement ─────────────────────────────────────────────────────
    // Not cached — takes month/year parameters, caching parameterized
    // results adds complexity without much benefit for this query
    // OWNER and MANAGER — both track rep targets
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public List<TargetAchievementDto> getTargetAchievement(int month, int year) {

        return callTargetRepository.findTargetAchievement(month, year)
                .stream()
                .map(p -> {

                    // Compute derived fields safely in Java
                    int remaining = p.getTargetVisits() - p.getActualVisits();

                    // achievementPct — guard against zero target
                    BigDecimal achievementPct = null;
                    if (p.getTargetVisits() > 0) {
                        achievementPct = BigDecimal
                                .valueOf(p.getActualVisits())
                                .multiply(BigDecimal.valueOf(100))
                                .divide(
                                        BigDecimal.valueOf(p.getTargetVisits()),
                                        2,
                                        RoundingMode.HALF_UP
                                );
                    }

                    // targetMet — true if actual >= target
                    boolean targetMet = p.getActualVisits() >= p.getTargetVisits();

                    return new TargetAchievementDto(
                            p.getRepId(),
                            p.getRepName(),
                            p.getMonth(),
                            p.getYear(),
                            p.getTargetVisits(),
                            p.getActualVisits(),
                            remaining,
                            achievementPct,
                            targetMet
                    );
                })
                .toList();
    }

    // ── Returns Summary ────────────────────────────────────────────────────────
    // Not cached — relatively fast grouped query
    // OWNER and MANAGER — both track returns
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public List<ReturnsSummaryDto> getReturnsSummary() {

        return returnRepository.findReturnsSummary()
                .stream()
                .map(p -> new ReturnsSummaryDto(
                        p.getMonth(),
                        p.getTotalReturnCount(),
                        p.getProcessedReturnCount(),
                        p.getRejectedReturnCount(),
                        p.getTotalReturnValue(),
                        p.getChemistReturnValue(),
                        p.getStockistReturnValue()
                ))
                .toList();
    }


    // ── AI Usage Cost Summary ──────────────────────────────────────────────────
    // Not cached — Owner needs real-time cost visibility
    // OWNER only — cost data is sensitive
    @PreAuthorize("hasRole('OWNER')")
    public java.util.Map<String, Object> getAiUsageSummary() {
        // Total cost across all time
        BigDecimal totalCost = aiInteractionRepository.getTotalCostAllTime();

        // Cost breakdown by feature
        java.util.Map<String, Long> featureCounts = new java.util.LinkedHashMap<>();
        for (String feature : new String[]{
                "DOCTOR_ENGAGEMENT", "VISIT_BRIEFING", "PAYMENT_RISK",
                "CHEMIST_PAYMENT_RISK", "TERRITORY_NARRATIVE",
                "ORDER_RECOMMENDATION", "PAYMENT_FOLLOW_UP"}) {
            featureCounts.put(feature,
                    aiInteractionRepository.getCallCountByFeature(feature));
        }

        return java.util.Map.of(
                "totalCostUsd", totalCost,
                "featureCallCounts", featureCounts,
                "currency", "USD"
        );
    }


}
