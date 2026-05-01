package org.ved.crm.chemist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateChemistRequest(
        @NotNull
        UUID assignedRepId,

        @NotBlank
        String firmName,

        @NotBlank
        String ownerName,

        // Drug License Number format: two digits, one letter, followed by digits
        // Example: MH-12345, or 20B/12345 — formats vary by state
        // We keep validation simple — just not blank, as DL formats differ across states
        @NotBlank
        String drugLicenseNumber,

        // GSTIN is optional — small chemists may be unregistered
        // But if provided, it must match the standard GST format
        @Pattern(
                regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
                message = "Invalid GSTIN format"
        )
        String gstin,

        @NotBlank
        String state,

        @NotBlank
        String city,

        String address,

        @NotBlank
        String phone
) {
}
