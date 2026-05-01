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

        // Chemist — always present
        UUID chemistId,
        String chemistFirmName,
        String chemistState,

        // Stockist — nullable for DIRECT orders
        UUID stockistId,
        String stockistFirmName,
        String stockistState,

        // Who is billed — STOCKIST or CHEMIST
        BilledTo billedTo,

        String invoiceNumber,
        LocalDate invoiceDate,
        TaxType taxType,
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal totalCgst,
        BigDecimal totalSgst,
        BigDecimal totalIgst,
        BigDecimal grandTotal,
        InvoiceStatus status,
        List<InvoiceLineItemDto> lineItems,
        Instant createdAt,
        Instant updatedAt
) {
}