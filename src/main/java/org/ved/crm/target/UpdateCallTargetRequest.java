package org.ved.crm.target;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCallTargetRequest(
        @NotNull @Min(1) Integer targetVisits,
        @NotNull @Min(0) Integer actualVisits
) {
}
