package org.ved.crm.Payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePaymentRequest(
        UUID stockistId,
        UUID chemistId,

        @NotNull
        LocalDate paymentDate,

        @NotNull
        @Positive
        BigDecimal amount,

        @NotNull
        PaymentMode paymentMode,

        String referenceNumber,

        String notes,

        @NotNull
        @NotEmpty
        @Valid
        List<PaymentAllocationRequest> allocations
) {
}
