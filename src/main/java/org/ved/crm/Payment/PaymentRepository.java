package org.ved.crm.Payment;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    @Query("""
            SELECT p FROM Payment p
            LEFT JOIN FETCH p.stockist s
            LEFT JOIN FETCH p.chemist c
            JOIN FETCH p.allocations a
            JOIN FETCH a.invoice i
            WHERE p.id = :id
            """)
    Optional<Payment> findByIdWithDetails(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT p FROM Payment p
            LEFT JOIN FETCH p.stockist s
            LEFT JOIN FETCH p.chemist c
            JOIN FETCH p.allocations a
            JOIN FETCH a.invoice i
            """)
    List<Payment> findAllWithDetails();

    // Get all payments made by a specific stockist
    @Query("""
            SELECT DISTINCT p FROM Payment p
            LEFT JOIN FETCH p.stockist s
            LEFT JOIN FETCH p.chemist c
            JOIN FETCH p.allocations a
            JOIN FETCH a.invoice i
            WHERE p.stockist.id = :stockistId
            """)
    List<Payment> findByStockistId(@Param("stockistId") UUID stockistId);

    // Get all payments made by a specific chemist
    @Query("""
            SELECT DISTINCT p FROM Payment p
            LEFT JOIN FETCH p.stockist s
            LEFT JOIN FETCH p.chemist c
            JOIN FETCH p.allocations a
            JOIN FETCH a.invoice i
            WHERE p.chemist.id = :chemistId
            """)
    List<Payment> findByChemistId(@Param("chemistId") UUID chemistId);

    // Sequential payment number from DB sequence
    @Query(value = "SELECT nextval('payment_number_seq')", nativeQuery = true)
    Long getNextSequenceValue();
}