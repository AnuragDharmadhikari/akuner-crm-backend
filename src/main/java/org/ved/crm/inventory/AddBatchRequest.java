package org.ved.crm.inventory;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AddBatchRequest(
        @NotNull UUID productId,
        @NotBlank String batchNumber,
        @NotNull LocalDate mfgDate,
        @NotNull @Future LocalDate expiryDate,
        @NotNull @Min(1) Integer quantity
        ) {
}
