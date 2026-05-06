package org.ved.crm.Payment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {

    // Get total amount already allocated to a specific invoice
    // Used to check how much is still outstanding on that invoice
    @Query(value = """
        SELECT COALESCE(SUM(allocated_amount), 0)
        FROM payment_allocations
        WHERE invoice_id = :invoiceId
        """, nativeQuery = true)
    BigDecimal getTotalAllocatedForInvoice(@Param("invoiceId") UUID invoiceId);
}
