package org.ved.crm.returns;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReturnItemRequest(

        // Which batch these units came from — batch level tracking
        @NotNull
        UUID batchId,

        // How many units being returned
        @NotNull
        @Min(1)
        Integer quantity,

        // SALEABLE → stock restored, DAMAGED/EXPIRED → written off
        @NotNull
        ReturnItemCondition condition
) {
}