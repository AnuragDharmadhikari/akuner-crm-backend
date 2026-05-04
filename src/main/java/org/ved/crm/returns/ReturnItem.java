package org.ved.crm.returns;

import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.common.audit.BaseAuditEntity;
import org.ved.crm.inventory.Batch;
import org.ved.crm.product.Product;

import java.math.BigDecimal;

@Entity
@Table(name = "return_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnItem extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private Return returnDoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false)
    private ReturnItemCondition condition;

    @Column(name = "unit_price", nullable = false,precision = 10,scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false,precision = 10, scale = 2)
    private BigDecimal lineTotal;


}
