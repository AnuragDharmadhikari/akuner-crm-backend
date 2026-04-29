package org.ved.crm.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemDto(
        UUID id,
        UUID productId,
        String productName,
        String hsnCode,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountPct,
        BigDecimal lineTotal
) {
}
