package org.ved.crm.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentAllocationDto(
        UUID id,
        UUID invoiceId,
        String invoiceNumber,
        BigDecimal invoiceGrandTotal,
        BigDecimal allocatedAmount,
        BigDecimal remainingAmount
) {
}
