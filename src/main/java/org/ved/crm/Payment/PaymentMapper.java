package org.ved.crm.Payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
@RequiredArgsConstructor
public class PaymentMapper {

    private final PaymentAllocationRepository paymentAllocationRepository;

    public PaymentDto toDto(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getStockist() != null ? payment.getStockist().getId() : null,
                payment.getStockist() != null ? payment.getStockist().getFirmName() : null,
                payment.getChemist() != null ? payment.getChemist().getId() : null,
                payment.getChemist() != null ? payment.getChemist().getFirmName() : null,
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getPaymentMode(),
                payment.getReferenceNumber(),
                payment.getNotes(),
                payment.getAllocations().stream()
                        .map(allocation -> {
                            // Get total allocated across ALL payments for this invoice
                            BigDecimal totalAllocated = paymentAllocationRepository
                                    .getTotalAllocatedForInvoice(
                                            allocation.getInvoice().getId());
                            return toAllocationDto(allocation, totalAllocated);
                        })
                        .toList(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private PaymentAllocationDto toAllocationDto(
            PaymentAllocation allocation, BigDecimal totalAllocated) {
        BigDecimal grandTotal = allocation.getInvoice().getGrandTotal();
        BigDecimal remaining = grandTotal.subtract(totalAllocated);

        return new PaymentAllocationDto(
                allocation.getId(),
                allocation.getInvoice().getId(),
                allocation.getInvoice().getInvoiceNumber(),
                grandTotal,
                allocation.getAllocatedAmount(),
                remaining.compareTo(BigDecimal.ZERO) < 0
                        ? BigDecimal.ZERO
                        : remaining
        );
    }
}