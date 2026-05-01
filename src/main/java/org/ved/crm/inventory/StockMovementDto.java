package org.ved.crm.inventory;

import java.time.Instant;
import java.util.UUID;

public record StockMovementDto(
        UUID id,
        UUID batchId,
        String batchNumber,
        UUID productId,
        String productName,
        MovementType movementType,
        Integer quantity,
        UUID referenceId,
        String referenceType,
        String notes,
        Instant createdAt
) {
}
