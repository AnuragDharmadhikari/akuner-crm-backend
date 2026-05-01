package org.ved.crm.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
}