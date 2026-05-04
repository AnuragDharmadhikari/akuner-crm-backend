package org.ved.crm.returns;

import org.springframework.stereotype.Component;

@Component
public class CreditNoteMapper {

    public CreditNoteDto toDto(CreditNote creditNote) {
        return new CreditNoteDto(
                creditNote.getId(),
                creditNote.getCreditNoteNumber(),

                // Which return generated this credit note
                creditNote.getReturnDoc().getId(),
                creditNote.getReturnDoc().getReturnNumber(),

                // Chemist — null safe
                creditNote.getChemist() != null
                        ? creditNote.getChemist().getId() : null,
                creditNote.getChemist() != null
                        ? creditNote.getChemist().getFirmName() : null,

                // Stockist — null safe
                creditNote.getStockist() != null
                        ? creditNote.getStockist().getId() : null,
                creditNote.getStockist() != null
                        ? creditNote.getStockist().getFirmName() : null,

                creditNote.getAmount(),
                creditNote.getStatus(),

                // Applied to invoice — null when OPEN, populated when APPLIED
                creditNote.getAppliedToInvoice() != null
                        ? creditNote.getAppliedToInvoice().getId() : null,
                creditNote.getAppliedToInvoice() != null
                        ? creditNote.getAppliedToInvoice().getInvoiceNumber() : null,

                creditNote.getCreatedAt(),
                creditNote.getUpdatedAt()
        );
    }
}