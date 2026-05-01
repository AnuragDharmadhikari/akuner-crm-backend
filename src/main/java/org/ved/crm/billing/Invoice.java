package org.ved.crm.billing;

import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.chemist.Chemist;
import org.ved.crm.common.audit.BaseAuditEntity;
import org.ved.crm.order.Order;
import org.ved.crm.stockist.Stockist;
import org.ved.crm.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "invoices")
public class Invoice extends BaseAuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rep_id",nullable = false)
    private User rep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chemist_id", nullable = false)
    private Chemist chemist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stockist_id")
    private Stockist stockist;

    // Who is being billed — drives tax state comparison
    // STOCKIST → tax based on stockist.state
    // CHEMIST → tax based on chemist.state
    @Enumerated(EnumType.STRING)
    @Column(name = "billed_to", nullable = false)
    private BilledTo billedTo;

    @Column(name = "invoice_number",nullable = false,unique = true)
    private String invoiceNumber;

    @Column(name = "invoice_date",nullable = false)
    private LocalDate invoiceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type",nullable = false)
    private TaxType taxType;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "total_discount", nullable = false,precision = 10,scale = 2)
    private BigDecimal totalDiscount;

    @Column(name = "total_cgst" , nullable = false,precision = 10,scale = 2)
    private BigDecimal totalCgst;

    @Column(name = "total_sgst" , nullable = false,precision = 10,scale = 2)
    private BigDecimal totalSgst;

    @Column(name = "total_igst" , nullable = false,precision = 10,scale = 2)
    private BigDecimal totalIgst;

    @Column(name = "grand_total" , nullable = false,precision = 10,scale = 2)
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL,orphanRemoval = true)
    @Builder.Default
    private List<InvoiceLineItem> lineItems = new ArrayList<>();

}
