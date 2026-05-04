package org.ved.crm.returns;

import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.billing.Invoice;
import org.ved.crm.chemist.Chemist;
import org.ved.crm.common.audit.BaseAuditEntity;
import org.ved.crm.stockist.Stockist;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_notes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditNote extends BaseAuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private Return returnDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chemist_id")
    private Chemist chemist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockist_id")
    private Stockist stockist;

    @Column(name = "credit_note_number", nullable = false, unique = true)
    private String creditNoteNumber;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CreditNoteStatus status = CreditNoteStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_to_invoice_id")
    private Invoice appliedToInvoice;

}
