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
    @Query("""
            SELECT COALESCE(SUM(pa.allocatedAmount), 0)
            FROM PaymentAllocation pa
            WHERE pa.invoice.id = :invoiceId
            """)
    BigDecimal getTotalAllocatedForInvoice(@Param("invoiceId") UUID invoiceId);
}
