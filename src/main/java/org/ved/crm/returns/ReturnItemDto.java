package org.ved.crm.returns;

import java.math.BigDecimal;
import java.util.UUID;

public record ReturnItemDto(
        UUID id,
        UUID batchId,
        String batchNumber,
        UUID productId,
        String productName,
        String hsnCode,
        Integer quantity,
        ReturnItemCondition condition,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
