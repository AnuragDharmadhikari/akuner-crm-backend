package org.ved.crm.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ved.crm.analytics.InvoiceProjections;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // JOIN FETCH chemist always — never null
    // LEFT JOIN FETCH stockist — null for DIRECT orders
    @Query("""
            SELECT i FROM Invoice i
            JOIN FETCH i.order o
            JOIN FETCH i.rep r
            JOIN FETCH i.chemist c
            LEFT JOIN FETCH i.stockist s
            JOIN FETCH i.lineItems li
            JOIN FETCH li.product p
            WHERE i.id = :id
            """)
    Optional<Invoice> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT i FROM Invoice i
            JOIN FETCH i.order o
            JOIN FETCH i.rep r
            JOIN FETCH i.chemist c
            LEFT JOIN FETCH i.stockist s
            JOIN FETCH i.lineItems li
            JOIN FETCH li.product p
            """)
    List<Invoice> findAllWithDetails();

    boolean existsByOrderId(UUID orderId);

    @Query(value = "SELECT nextval('invoice_number_seq')", nativeQuery = true)
    Long getNextSequenceValue();

    // ── ANALYTICS: Revenue Summary
    @Query(value = """
        SELECT
            TO_CHAR(i.created_at, 'YYYY-MM')            AS month,
            SUM(i.grand_total)                           AS total_revenue,
            COUNT(i.id)                                  AS invoice_count,
            ROUND(AVG(i.grand_total), 2)                 AS average_invoice_value
        FROM invoices i
        WHERE i.status IN ('ISSUED', 'PARTIALLY_PAID', 'PAID')
        GROUP BY TO_CHAR(i.created_at, 'YYYY-MM')
        ORDER BY month ASC
        """, nativeQuery = true)
    List<InvoiceProjections.RevenueSummaryProjection> findRevenueSummary();

    // ── ANALYTICS: GST Liability
    @Query(value = """
        SELECT
            TO_CHAR(i.created_at, 'YYYY-MM')                        AS month,
            COALESCE(SUM(il.cgst_amt), 0)                           AS total_cgst,
            COALESCE(SUM(il.sgst_amt), 0)                           AS total_sgst,
            COALESCE(SUM(il.igst_amt), 0)                           AS total_igst,
            COALESCE(SUM(il.cgst_amt + il.sgst_amt + il.igst_amt), 0) AS total_tax_liability
        FROM invoices i
        JOIN invoice_line_items il ON il.invoice_id = i.id
        WHERE i.status IN ('ISSUED', 'PARTIALLY_PAID', 'PAID')
        GROUP BY TO_CHAR(i.created_at, 'YYYY-MM')
        ORDER BY month ASC
        """, nativeQuery = true)
    List<InvoiceProjections.GstLiabilityProjection> findGstLiability();

    // ── ANALYTICS: Outstanding Invoices
    @Query(value = """
        SELECT
            i.id                                                        AS invoice_id,
            i.invoice_number                                            AS invoice_number,
            COALESCE(s.firm_name, c.firm_name)                          AS billed_to_name,
            i.grand_total                                               AS grand_total,
            COALESCE(SUM(DISTINCT pa.allocated_amount), 0)             AS total_paid,
            COALESCE(SUM(DISTINCT cn.amount) FILTER (
                WHERE cn.status = 'APPLIED'
            ), 0)                                                       AS total_credit_applied,
            i.grand_total
                - COALESCE(SUM(DISTINCT pa.allocated_amount), 0)
                - COALESCE(SUM(DISTINCT cn.amount) FILTER (
                    WHERE cn.status = 'APPLIED'
                ), 0)                                                   AS outstanding_amount,
            i.status                                                    AS status,
            EXTRACT(DAY FROM NOW() - i.created_at)::BIGINT             AS days_since_issued
        FROM invoices i
        LEFT JOIN stockists s ON s.id = i.stockist_id
        LEFT JOIN chemists c  ON c.id = i.chemist_id
        LEFT JOIN payment_allocations pa ON pa.invoice_id = i.id
        LEFT JOIN credit_notes cn        ON cn.applied_to_invoice_id = i.id
        WHERE i.status IN ('ISSUED', 'PARTIALLY_PAID')
        GROUP BY i.id, i.invoice_number, i.grand_total, i.status,
                 i.created_at, s.firm_name, c.firm_name
        HAVING (
            i.grand_total
            - COALESCE(SUM(DISTINCT pa.allocated_amount), 0)
            - COALESCE(SUM(DISTINCT cn.amount) FILTER (
                WHERE cn.status = 'APPLIED'
            ), 0)
        ) > 0
        ORDER BY days_since_issued DESC
        """, nativeQuery = true)
    List<InvoiceProjections.OutstandingInvoiceProjection> findOutstandingInvoices();

    // ── ANALYTICS: Top Stockists by Revenue
    @Query(value = """

            SELECT
            s.id                    AS id,
            s.firm_name             AS name,
            s.state                 AS state,
            SUM(i.grand_total)      AS total_revenue,
            COUNT(i.id)             AS invoice_count
        FROM invoices i
        JOIN stockists s ON s.id = i.stockist_id
        WHERE i.status IN ('ISSUED', 'PARTIALLY_PAID', 'PAID')
          AND i.billed_to = 'STOCKIST'
        GROUP BY s.id, s.firm_name, s.state
        ORDER BY total_revenue DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<InvoiceProjections.TopStockistProjection> findTopStockists(@Param("limit") int limit);

    // ── ANALYTICS: Top Chemists by Revenue
    @Query(value = """

            SELECT
            c.id                    AS id,
            c.firm_name             AS name,
            c.state                 AS state,
            SUM(i.grand_total)      AS total_revenue,
            COUNT(i.id)             AS invoice_count
        FROM invoices i
        JOIN chemists c ON c.id = i.chemist_id
        WHERE i.status IN ('ISSUED', 'PARTIALLY_PAID', 'PAID')
          AND i.billed_to = 'CHEMIST'
        GROUP BY c.id, c.firm_name, c.state
        ORDER BY total_revenue DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<InvoiceProjections.TopChemistProjection> findTopChemists(@Param("limit") int limit);

    // ── ANALYTICS: Product Velocity
    @Query(value = """
        SELECT
            p.id                                                    AS product_id,
            p.name                                                  AS product_name,
            p.molecule                                              AS molecule,
            p.hsn_code                                              AS hsn_code,
            SUM(il.quantity)                                        AS total_units_sold,
            COALESCE(SUM(il.free_quantity), 0)                      AS total_free_units,
            SUM(il.quantity) + COALESCE(SUM(il.free_quantity), 0)  AS total_units_deducted,
            ROUND(SUM(il.line_total), 2)                            AS total_revenue
        FROM invoice_line_items il
        JOIN invoices i  ON i.id  = il.invoice_id
        JOIN products p  ON p.id  = il.product_id
        WHERE i.status IN ('ISSUED', 'PARTIALLY_PAID', 'PAID')
        GROUP BY p.id, p.name, p.molecule, p.hsn_code
        ORDER BY total_units_sold DESC
        """, nativeQuery = true)
    List<InvoiceProjections.ProductVelocityProjection> findProductVelocity();
}