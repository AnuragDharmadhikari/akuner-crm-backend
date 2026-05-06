package org.ved.crm.visit;

import java.util.UUID;

public record VisitProductDto(
        UUID id,
        UUID productId,
        String productName,
        String hsnCode,

        // Batch from which samples were distributed.
        // Null when no samples were given on this visit product.
        UUID batchId,
        String batchNumber,

        Integer samplesGiven,
        String feedback
) {}