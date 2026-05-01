package org.ved.crm.target;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCallTargetRequest(
        @NotNull UUID repId,
        @NotNull UUID assignedById,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotNull @Min(2020) @Max(2100) Integer year,
        @NotNull @Min(1) Integer targetVisits
) {
}
