package org.ved.crm.Payment;

import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.chemist.Chemist;
import org.ved.crm.common.audit.BaseAuditEntity;
import org.ved.crm.stockist.Stockist;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockist_id")
    private Stockist stockist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chemist_id")
    private Chemist chemist;

    @Column(name = "payment_number",nullable = false,unique = true)
    private String paymentNumber;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "amount", nullable = false,precision = 10,scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode",nullable = false)
    private PaymentMode paymentMode;

    @Column(name = "reference_number")
    private String referenceNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // One payment can be allocated to multiple invoices
    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PaymentAllocation> allocations = new ArrayList<>();
}
