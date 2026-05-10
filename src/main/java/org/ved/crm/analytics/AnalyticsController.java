package org.ved.crm.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ved.crm.common.ApiResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/revenue/summary")
    public ResponseEntity<ApiResponse<List<RevenueSummaryDto>>> getRevenueSummary(){
        return ResponseEntity.ok(ApiResponse.success("Revenue summary retrieved successfully",analyticsService.getRevenueSummary()));
    }

    @GetMapping("/gst/liability")
    public ResponseEntity<ApiResponse<List<GstLiabilityDto>>> getGstLiability() {
        return ResponseEntity.ok(ApiResponse.success(
                "GST liability retrieved successfully",
                analyticsService.getGstLiability()
        ));
    }

    @GetMapping("/invoices/outstanding")
    public ResponseEntity<ApiResponse<List<OutstandingInvoiceDto>>> getOutstandingInvoices() {
        return ResponseEntity.ok(ApiResponse.success(
                "Outstanding invoices retrieved successfully",
                analyticsService.getOutstandingInvoices()
        ));
    }

    @GetMapping("/credit-notes/open-total")
    public ResponseEntity<ApiResponse<OpenCreditNoteTotalDto>> getOpenCreditNoteTotal() {
        return ResponseEntity.ok(ApiResponse.success(
                "Open credit note total retrieved successfully",
                analyticsService.getOpenCreditNoteTotal()
        ));
    }

    @GetMapping("/stockists/top")
    public ResponseEntity<ApiResponse<List<TopPerformerDto>>> getTopStockists(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                "Top stockists retrieved successfully",
                analyticsService.getTopStockists(limit)
        ));
    }

    @GetMapping("/chemists/top")
    public ResponseEntity<ApiResponse<List<TopPerformerDto>>> getTopChemists(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                "Top chemists retrieved successfully",
                analyticsService.getTopChemists(limit)
        ));
    }

    @GetMapping("/reps/performance")
    public ResponseEntity<ApiResponse<List<RepPerformanceDto>>> getRepPerformance() {
        return ResponseEntity.ok(ApiResponse.success(
                "Rep performance retrieved successfully",
                analyticsService.getRepPerformance()
        ));
    }

    @GetMapping("/products/velocity")
    public ResponseEntity<ApiResponse<List<ProductVelocityDto>>> getProductVelocity() {
        return ResponseEntity.ok(ApiResponse.success(
                "Product velocity retrieved successfully",
                analyticsService.getProductVelocity()
        ));
    }

    @GetMapping("/inventory/value")
    public ResponseEntity<ApiResponse<List<InventoryValueDto>>> getInventoryValue() {
        return ResponseEntity.ok(ApiResponse.success(
                "Inventory value retrieved successfully",
                analyticsService.getInventoryValue()
        ));
    }

    @GetMapping("/inventory/near-expiry-value")
    public ResponseEntity<ApiResponse<List<NearExpiryValueDto>>> getNearExpiryValue() {
        return ResponseEntity.ok(ApiResponse.success(
                "Near expiry inventory retrieved successfully",
                analyticsService.getNearExpiryValue()
        ));
    }

    @GetMapping("/targets/achievement")
    public ResponseEntity<ApiResponse<List<TargetAchievementDto>>> getTargetAchievement(
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "0") int year) {

        // If month or year not provided (defaulted to 0), use current month/year
        // This is cleaner than defaultValue = "5" which would hardcode the month
        int resolvedMonth = month == 0 ? LocalDate.now().getMonthValue() : month;
        int resolvedYear  = year  == 0 ? LocalDate.now().getYear()       : year;

        return ResponseEntity.ok(ApiResponse.success(
                "Target achievement retrieved successfully",
                analyticsService.getTargetAchievement(resolvedMonth, resolvedYear)
        ));
    }

    @GetMapping("/returns/summary")
    public ResponseEntity<ApiResponse<List<ReturnsSummaryDto>>> getReturnsSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                "Returns summary retrieved successfully",
                analyticsService.getReturnsSummary()
        ));
    }

    // ── AI Usage Cost Summary ──────────────────────────────────────────────────
// GET /api/v1/analytics/ai/usage
// OWNER only
    @GetMapping("/ai/usage")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getAiUsageSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                "AI usage summary retrieved successfully",
                analyticsService.getAiUsageSummary()
        ));
    }

}
