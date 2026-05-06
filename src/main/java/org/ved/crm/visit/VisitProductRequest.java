package org.ved.crm.visit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VisitProductRequest(

        @NotNull UUID productId,

        // Which batch samples were pulled from.
        // Required when samplesGiven > 0 — service enforces this.
        // Null when rep is only pitching the product, no samples given.
        UUID batchId,

        // How many sample units given to the doctor.
        // Null means no samples — rep just detailed the product.
        @Min(value = 1, message = "Samples given must be at least 1 if provided")
        Integer samplesGiven,

        String feedback
) {}