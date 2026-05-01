package org.ved.crm.target;

import java.time.Instant;
import java.util.UUID;

public record CallTargetDto(
        UUID id,
        UUID repId,
        String repName,
        UUID assignedById,
        String assignedByName,
        Integer month,
        Integer year,
        Integer targetVisits,
        Integer actualVisits,
        double achievementPct,
        Instant createdAt,
        Instant updatedAt
) {
}
