package org.ved.crm.analytics;

import java.math.BigDecimal;
import java.util.UUID;

public class InvoiceProjections {

    public interface RevenueSummaryProjection{
        String getMonth();
        BigDecimal getTotalRevenue();
        Long getInvoiceCount();
        BigDecimal getAverageInvoiceValue();
    }

    public interface GstLiabilityProjection {
        String getMonth();
        BigDecimal getTotalCgst();
        BigDecimal getTotalSgst();
        BigDecimal getTotalIgst();
        BigDecimal getTotalTaxLiability();
    }

    public interface OutstandingInvoiceProjection{
        UUID getInvoiceId();
        String getInvoiceNumber();
        String getBilledToName();
        BigDecimal getGrandTotal();
        BigDecimal getTotalPaid();
        BigDecimal getTotalCreditApplied();
        BigDecimal getOutstandingAmount();
        String getStatus();
        Long getDaysSinceIssued();
    }

    public interface TopStockistProjection{
        UUID getId();
        String getName();
        String getState();
        BigDecimal getTotalRevenue();
        Long getInvoiceCount();
    }

    public interface TopChemistProjection{
        UUID getId();
        String getName();
        String getState();
        BigDecimal getTotalRevenue();
        Long getInvoiceCount();
    }

    // ── Product Velocity ───────────────────────────────────────────────────────
    // Used by: GET /analytics/products/velocity
    // One row per product — aggregates units and revenue across all invoices
    public interface ProductVelocityProjection {
        UUID getProductId();            // p.id AS product_id
        String getProductName();        // p.name AS product_name
        String getMolecule();           // p.molecule AS molecule
        String getHsnCode();            // p.hsn_code AS hsn_code
        Long getTotalUnitsSold();       // SUM(il.quantity) AS total_units_sold
        Long getTotalFreeUnits();       // SUM(il.free_quantity) AS total_free_units
        Long getTotalUnitsDeducted();   // SUM(il.quantity + il.free_quantity)
        BigDecimal getTotalRevenue();   // SUM(il.line_total) AS total_revenue
    }

}
