package org.akuner.crm.returns;

import jakarta.persistence.*;
import lombok.*;
import org.akuner.crm.common.audit.BaseAuditEntity;
import org.akuner.crm.inventory.Batch;
import org.akuner.crm.product.Product;

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
