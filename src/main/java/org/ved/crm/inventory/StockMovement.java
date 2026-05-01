package org.ved.crm.inventory;


import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.common.audit.BaseAuditEntity;

import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id",nullable = false)
    private Batch batch;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type",nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Reference to the document that caused this movement
    // e.g. invoice ID for SALE, return ID for RETURN
    @Column(name = "reference_id")
    private UUID referenceId;

    // What type of document caused this movement
    // e.g. "INVOICE", "RETURN", "MANUAL"
    @Column(name = "reference_type")
    private String referenceType;

    // Optional notes for manual adjustments
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

}
