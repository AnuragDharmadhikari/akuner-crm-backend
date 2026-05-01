package org.ved.crm.inventory;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BatchDto(
        UUID id,
        UUID productId,
        String productName,
        String hsnCode,
        String batchNumber,
        LocalDate mfgDate,
        LocalDate expiryDate,
        Integer initialQuantity,
        Integer currentQuantity,
        boolean isExpired,
        boolean isNearExpiry,
        Instant createdAt,
        Instant updatedAt
) {
}
