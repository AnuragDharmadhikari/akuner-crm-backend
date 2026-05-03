package org.ved.crm.Payment;

import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.billing.Invoice;
import org.ved.crm.common.audit.BaseAuditEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAllocation extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id",nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id",nullable = false)
    private Invoice invoice;

    @Column(name = "allocated_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal allocatedAmount;

}
