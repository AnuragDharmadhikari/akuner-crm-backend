package org.ved.crm.analytics;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public record OutstandingInvoiceDto(

        UUID invoiceId,

        String invoiceNumber,

        String billedToName,

        BigDecimal grandTotal,

        BigDecimal totalPaid,

        BigDecimal totalCreditApplied,

        BigDecimal outstandingAmount,

        String status,

        long daysSinceIssued

) implements Serializable {
}
