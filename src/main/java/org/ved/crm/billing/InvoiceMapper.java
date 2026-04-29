package org.ved.crm.billing;

import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceDto toDto(Invoice invoice){

        return new InvoiceDto(
                invoice.getId(),
                invoice.getOrder().getId(),
                invoice.getRep().getId(),
                invoice.getRep().getFullName(),
                invoice.getInvoiceNumber(),
                invoice.getInvoiceDate(),
                invoice.getTaxType(),
                invoice.getSubtotal(),
                invoice.getTotalDiscount(),
                invoice.getTotalCgst(),
                invoice.getTotalSgst(),
                invoice.getTotalIgst(),
                invoice.getGrandTotal(),
                invoice.getStatus(),
                invoice.getLineItems().stream()
                        .map(this::toLineItemDto)
                        .toList(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );

    }// Maps a single InvoiceLineItem to InvoiceLineItemDto
    private InvoiceLineItemDto toLineItemDto(InvoiceLineItem item) {
        return new InvoiceLineItemDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getHsnCode(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getDiscountPct(),
                item.getTaxableAmount(),
                item.getCgstAmt(),
                item.getSgstAmt(),
                item.getIgstAmt(),
                item.getLineTotal()
        );
    }
}
