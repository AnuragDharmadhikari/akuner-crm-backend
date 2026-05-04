package org.ved.crm.returns;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreditNoteDto(
        UUID id,
        String creditNoteNumber,

        // Which return generated this credit note
        UUID returnId,
        String returnNumber,

        // Who gets the credit — one will be null
        UUID chemistId,
        String chemistFirmName,
        UUID stockistId,
        String stockistFirmName,

        BigDecimal amount,
        CreditNoteStatus status,

        // Null until credit note is applied to an invoice
        UUID appliedToInvoiceId,
        String appliedToInvoiceNumber,

        Instant createdAt,
        Instant updatedAt
) {
}