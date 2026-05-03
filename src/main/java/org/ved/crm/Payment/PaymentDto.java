package org.ved.crm.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PaymentDto(
        UUID id,
        String paymentNumber,
        UUID stockistId,
        String stockistFirmName,
        UUID chemistId,
        String chemistFirmName,
        LocalDate paymentDate,
        BigDecimal amount,
        PaymentMode paymentMode,
        String referenceNumber,
        String notes,
        List<PaymentAllocationDto> allocations,
        Instant createdAt,
        Instant updatedAt
) {
}
