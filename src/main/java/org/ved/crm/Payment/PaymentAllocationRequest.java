package org.ved.crm.Payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAllocationRequest(

        @NotNull
        UUID invoiceId,

        @NotNull
        @Positive
        BigDecimal allocatedAmount

) {
}
