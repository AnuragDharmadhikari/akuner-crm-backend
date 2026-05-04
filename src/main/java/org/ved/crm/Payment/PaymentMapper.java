package org.ved.crm.Payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.ved.crm.returns.CreditNoteRepository;

import java.math.BigDecimal;
@Component
@RequiredArgsConstructor
public class PaymentMapper {

    private final PaymentAllocationRepository paymentAllocationRepository;
    private final CreditNoteRepository creditNoteRepository;

    public PaymentDto toDto(Payment payment) {
        return new PaymentDto(
                payment.getId(),
                payment.getPaymentNumber(),

                // Stockist — null safe
                payment.getStockist() != null ? payment.getStockist().getId() : null,
                payment.getStockist() != null ? payment.getStockist().getFirmName() : null,

                // Chemist — null safe
                payment.getChemist() != null ? payment.getChemist().getId() : null,
                payment.getChemist() != null ? payment.getChemist().getFirmName() : null,

                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getPaymentMode(),
                payment.getReferenceNumber(),
                payment.getNotes(),
                payment.getAllocations().stream()
                        .map(allocation -> {
                            // Total paid via payments across all time
                            BigDecimal totalPaid = paymentAllocationRepository
                                    .getTotalAllocatedForInvoice(
                                            allocation.getInvoice().getId());

                            // Total credited via credit notes across all time
                            BigDecimal totalCredited = creditNoteRepository
                                    .getTotalCreditAppliedForInvoice(
                                            allocation.getInvoice().getId());

                            return toAllocationDto(allocation, totalPaid, totalCredited);
                        })
                        .toList(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }

    private PaymentAllocationDto toAllocationDto(
            PaymentAllocation allocation,
            BigDecimal totalPaid,
            BigDecimal totalCredited) {

        BigDecimal grandTotal = allocation.getInvoice().getGrandTotal();

        // Remaining = grandTotal - all payments - all credits
        BigDecimal remaining = grandTotal
                .subtract(totalPaid)
                .subtract(totalCredited);

        return new PaymentAllocationDto(
                allocation.getId(),
                allocation.getInvoice().getId(),
                allocation.getInvoice().getInvoiceNumber(),
                grandTotal,
                allocation.getAllocatedAmount(),
                // Never show negative remaining — floor at zero
                remaining.compareTo(BigDecimal.ZERO) < 0
                        ? BigDecimal.ZERO
                        : remaining
        );
    }
}