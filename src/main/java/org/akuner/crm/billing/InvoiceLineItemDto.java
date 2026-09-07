package org.akuner.crm.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record InvoiceLineItemDto(
        UUID id,
        UUID productId,
        String productName,
        String hsnCode,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountPct,
        BigDecimal taxableAmount,
        BigDecimal cgstAmt,
        BigDecimal sgstAmt,
        BigDecimal igstAmt,
        Integer freeQuantity,
        BigDecimal lineTotal,
        String batchNumber,
        LocalDate expiryDate
) {}