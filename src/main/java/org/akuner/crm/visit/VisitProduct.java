package org.akuner.crm.visit;

import jakarta.persistence.*;
import lombok.*;
import org.akuner.crm.common.audit.BaseAuditEntity;
import org.akuner.crm.inventory.Batch;
import org.akuner.crm.product.Product;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "visit_products")
public class VisitProduct extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Which batch the samples were pulled from.
    // Nullable — rep may pitch a product without giving samples.
    // When samplesGiven > 0, this must be non-null (service enforces).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private Batch batch;

    // How many sample units were given to the doctor.
    // Nullable — zero samples is valid, rep just pitched the product.
    @Column(name = "samples_given")
    private Integer samplesGiven;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}