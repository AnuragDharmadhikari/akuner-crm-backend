package org.ved.crm.billing;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record InvoiceDto(
        UUID id,
        UUID orderId,
        UUID repId,
        String repName,
        String invoiceNumber,
        LocalDate invoiceDate,
        TaxType taxType,
        BigDecimal subTotal,
        BigDecimal totalDiscount,
        BigDecimal totalCgst,
        BigDecimal totalSgst,
        BigDecimal totalIgst,
        BigDecimal grandTotal,
        InvoiceStatus status,
        List<InvoiceLineItemDto> lineItems,
        Instant createAt,
        Instant updatedAt
) {
}
