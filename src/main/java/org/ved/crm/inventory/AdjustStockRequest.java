package org.ved.crm.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdjustStockRequest(
        // Can be positive (stock in) or negative (stock out)
        // No @Min constraint — owner can adjust in either direction
        @NotNull
        Integer quantity,

        // Owner must always give a reason for manual adjustment
        // This goes into StockMovement.notes for audit trail
        @NotBlank
        String reason

) {
}
