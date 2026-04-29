package org.ved.crm.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    // Fetches invoice with all relationships needed by the mapper in one query
    // JOIN FETCH on order, rep, and lineItems, then JOIN FETCH product inside lineItems
    @Query("""
            SELECT i FROM Invoice i
            JOIN FETCH i.order o
            JOIN FETCH i.rep r
            JOIN FETCH i.lineItems li
            JOIN FETCH li.product p
            WHERE i.id = :id
            """)
    Optional<Invoice> findByIdWithDetails(@Param("id") UUID id);

    // Get all invoices with full details for the list endpoint
    @Query("""
            SELECT DISTINCT i FROM Invoice i
            JOIN FETCH i.order o
            JOIN FETCH i.rep r
            JOIN FETCH i.lineItems li
            JOIN FETCH li.product p
            """)
    List<Invoice> findAllWithDetails();

    // Check if an invoice already exists for this order — one order = one invoice
    boolean existsByOrderId(UUID orderId);

    // Calls PostgreSQL SEQUENCE directly — nativeQuery=true because
    // nextval() is PostgreSQL specific and JPQL does not understand it
    @Query(value = "SELECT nextval('invoice_number_seq')", nativeQuery = true)
    Long getNextSequenceValue();
}