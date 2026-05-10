package org.ved.crm.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ved.crm.ai.dto.*;
import org.ved.crm.common.ApiResponse;

import java.util.UUID;

// Thin HTTP layer — zero business logic
// All logic, authorization, and caching in AiService
// All endpoints are GET — AI features are read-only intelligence
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    // ── Feature 1: Doctor Engagement Score ────────────────────────────────────
    // GET /api/v1/ai/doctors/{id}/engagement
    // Cached 24h per doctor in Redis
    // OWNER, MANAGER, REP
    @GetMapping("/doctors/{id}/engagement")
    public ResponseEntity<ApiResponse<DoctorEngagementDto>> getDoctorEngagement(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Doctor engagement analysis retrieved successfully",
                aiService.getDoctorEngagement(id)
        ));
    }

    // ── Feature 2: Visit Briefing ──────────────────────────────────────────────
    // GET /api/v1/ai/visits/{id}/briefing
    // Cached 1h per visit in Redis
    // OWNER, MANAGER, REP
    @GetMapping("/visits/{id}/briefing")
    public ResponseEntity<ApiResponse<VisitBriefingDto>> getVisitBriefing(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Visit briefing retrieved successfully",
                aiService.getVisitBriefing(id)
        ));
    }

    // ── Feature 3: Stockist Payment Risk ──────────────────────────────────────
    // GET /api/v1/ai/stockists/{id}/payment-risk
    // Cached 6h per stockist in Redis
    // OWNER only
    @GetMapping("/stockists/{id}/payment-risk")
    public ResponseEntity<ApiResponse<PaymentRiskDto>> getStockistPaymentRisk(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Stockist payment risk analysis retrieved successfully",
                aiService.getStockistPaymentRisk(id)
        ));
    }

    // ── Feature 4: Territory Narrative ────────────────────────────────────────
    // GET /api/v1/ai/territories/{id}/narrative
    // Cached 12h per territory in Redis
    // OWNER, MANAGER
    @GetMapping("/territories/{id}/narrative")
    public ResponseEntity<ApiResponse<TerritoryNarrativeDto>> getTerritoryNarrative(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Territory narrative retrieved successfully",
                aiService.getTerritoryNarrative(id)
        ));
    }

    // ── Feature 5: Smart Order Recommendation ─────────────────────────────────
    // GET /api/v1/ai/orders/recommend/{chemistId}
    // Cached 2h per chemist in Redis
    // OWNER, REP
    @GetMapping("/orders/recommend/{chemistId}")
    public ResponseEntity<ApiResponse<OrderRecommendationDto>> getOrderRecommendation(
            @PathVariable UUID chemistId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Order recommendation retrieved successfully",
                aiService.getOrderRecommendation(chemistId)
        ));
    }

    // ── Feature 6: Payment Follow-up Message ──────────────────────────────────
    // GET /api/v1/ai/invoices/{id}/follow-up
    // NOT cached — always fresh
    // OWNER only
    @GetMapping("/invoices/{id}/follow-up")
    public ResponseEntity<ApiResponse<PaymentFollowUpDto>> getPaymentFollowUp(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment follow-up message generated successfully",
                aiService.getPaymentFollowUp(id)
        ));
    }

    @GetMapping("/chemists/{id}/payment-risk")
    public ResponseEntity<ApiResponse<PaymentRiskDto>> getChemistPaymentRisk(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Chemist payment risk analysis retrieved successfully",
                aiService.getChemistPaymentRisk(id)
        ));
    }

}