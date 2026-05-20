package org.ved.crm.billing;

import java.math.BigDecimal;
import java.util.UUID;

public record OutstandingInvoiceDto(
        UUID invoiceId,
        String invoiceNumber,
        String billedToName,
        BigDecimal grandTotal,
        BigDecimal totalPaid,
        BigDecimal outstandingAmount,
        String status,
        UUID chemistId,
        UUID stockistId
) {}