package org.ved.crm.returns;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ved.crm.analytics.CreditNoteProjections;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditNoteRepository extends JpaRepository<CreditNote, UUID> {

    // Load credit note with all relationships
    @Query("""
            SELECT cn FROM CreditNote cn
            LEFT JOIN FETCH cn.chemist c
            LEFT JOIN FETCH cn.stockist s
            JOIN FETCH cn.returnDoc r
            LEFT JOIN FETCH cn.appliedToInvoice i
            WHERE cn.id = :id
            """)
    Optional<CreditNote> findByIdWithDetails(@Param("id") UUID id);

    // Get all credit notes
    @Query("""
            SELECT DISTINCT cn FROM CreditNote cn
            LEFT JOIN FETCH cn.chemist c
            LEFT JOIN FETCH cn.stockist s
            JOIN FETCH cn.returnDoc r
            LEFT JOIN FETCH cn.appliedToInvoice i
            """)
    List<CreditNote> findAllWithDetails();

    // Get all OPEN credit notes for a chemist
    // Used when checking available credits before raising new invoice
    @Query("""
            SELECT cn FROM CreditNote cn
            LEFT JOIN FETCH cn.chemist c
            LEFT JOIN FETCH cn.stockist s
            JOIN FETCH cn.returnDoc r
            LEFT JOIN FETCH cn.appliedToInvoice i
            WHERE cn.chemist.id = :chemistId
            AND cn.status = 'OPEN'
            """)
    List<CreditNote> findOpenCreditNotesByChemist(@Param("chemistId") UUID chemistId);

    // Get all OPEN credit notes for a stockist
    @Query("""
            SELECT cn FROM CreditNote cn
            LEFT JOIN FETCH cn.chemist c
            LEFT JOIN FETCH cn.stockist s
            JOIN FETCH cn.returnDoc r
            LEFT JOIN FETCH cn.appliedToInvoice i
            WHERE cn.stockist.id = :stockistId
            AND cn.status = 'OPEN'
            """)
    List<CreditNote> findOpenCreditNotesByStockist(@Param("stockistId") UUID stockistId);

    // Get total credit note amount applied to a specific invoice
    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM credit_notes
        WHERE applied_to_invoice_id = :invoiceId
        AND status = 'APPLIED'
        """, nativeQuery = true)
    BigDecimal getTotalCreditAppliedForInvoice(@Param("invoiceId") UUID invoiceId);

    // Sequential credit note number from PostgreSQL SEQUENCE
    @Query(value = "SELECT nextval('credit_note_number_seq')", nativeQuery = true)
    Long getNextSequenceValue();

    @Query(value = """
        SELECT
            COALESCE(SUM(cn.amount), 0)
                                                            AS total_open_value,
            COUNT(cn.id)
                                                            AS open_count,
            COALESCE(SUM(cn.amount) FILTER (
                WHERE cn.stockist_id IS NOT NULL
            ), 0)                                           AS stockist_open_value,
            COALESCE(SUM(cn.amount) FILTER (
                WHERE cn.chemist_id IS NOT NULL
            ), 0)                                           AS chemist_open_value
        FROM credit_notes cn
        WHERE cn.status = 'OPEN'
        """, nativeQuery = true)
    CreditNoteProjections.OpenCreditNoteTotalProjection findOpenCreditNoteTotal();

}