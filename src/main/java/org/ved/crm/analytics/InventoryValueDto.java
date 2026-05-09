package org.ved.crm.analytics;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryValueDto(

        UUID productId,

        String productName,

        String hsnCode,

        BigDecimal dealerPrice,

        long totalCurrentUnits,

        BigDecimal totalInventoryValue

) {
}
