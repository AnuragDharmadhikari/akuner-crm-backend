package org.ved.crm.Payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.billing.Invoice;
import org.ved.crm.billing.InvoiceRepository;
import org.ved.crm.billing.InvoiceStatus;
import org.ved.crm.chemist.Chemist;
import org.ved.crm.chemist.ChemistRepository;
import org.ved.crm.common.exception.ResourceNotFoundException;
import org.ved.crm.stockist.Stockist;
import org.ved.crm.stockist.StockistRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private  final PaymentAllocationRepository paymentAllocationRepository;
    private final InvoiceRepository invoiceRepository;
    private final StockistRepository stockistRepository;
    private final ChemistRepository chemistRepository;
    private final PaymentMapper paymentMapper;

    // GET all payments
    public List<PaymentDto> getAllPayments(){
        return paymentRepository.findAllWithDetails()
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    // GET payment by ID
    public PaymentDto getPaymentById(UUID id){
        Payment payment = paymentRepository.findByIdWithDetails(id)
                .orElseThrow(()->new ResourceNotFoundException("Payment","id",id));
        return paymentMapper.toDto(payment);
    }

    // GET payments by stockist
    public List<PaymentDto> getPaymentsByStockist(UUID stockistId){
        return paymentRepository.findByStockistId(stockistId)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    // GET payments by chemist
    public List<PaymentDto> getPaymentsByChemist(UUID chemistId) {
        return paymentRepository.findByChemistId(chemistId)
                .stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    // CREATE payment — the most complex method

    @Transactional
    public PaymentDto createPayment(CreatePaymentRequest request){

        // Step 1 — Validate payer
        // Exactly one of stockistId or chemistId must be provided
        if(request.stockistId() == null && request.chemistId() == null){
            throw new IllegalArgumentException("Either stockistId or chemistId must be provided");
        }

        if(request.stockistId() != null && request.chemistId() != null){
            throw new IllegalArgumentException("Only one of stockistId or chemistId can be provided");
        }

        // Step 2 — Load the payer entity
        Stockist stockist = null;
        Chemist chemist = null;

        if(request.stockistId() != null){
            stockist = stockistRepository.findByIdWithDetails(request.stockistId())
                    .orElseThrow(()->new ResourceNotFoundException("Stockist","id",request.stockistId()));
        }else {
            chemist = chemistRepository.findByIdWithDetails(request.chemistId())
                    .orElseThrow(()->new ResourceNotFoundException("Chemist","id",request.chemistId()));
        }

        // Step 3 — Validate total allocation equals payment amount
        // Sum of all allocatedAmounts must equal the payment amount
        // You cannot allocate more or less than what was received

        BigDecimal totalAllocated = request.allocations().stream()
                .map(PaymentAllocationRequest::allocatedAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        if(totalAllocated.compareTo(request.amount()) != 0 ){
            throw new IllegalArgumentException(
                    "Total allocated amount (" + totalAllocated
                            + ") must equal payment amount (" + request.amount() + ")");
        }

        // Step 4 — Validate each invoice allocation
        // For each invoice, check:
        // a) Invoice exists
        // b) Invoice is ISSUED or PARTIALLY_PAID — cannot pay DRAFT or already PAID
        // c) Allocation does not exceed outstanding balance on that invoice

        for(PaymentAllocationRequest allocationReq : request.allocations()){

            Invoice invoice = invoiceRepository.findByIdWithDetails(allocationReq.invoiceId())
                    .orElseThrow(()->new ResourceNotFoundException("Invoice","id",allocationReq.invoiceId()));

            // Cannot pay a DRAFT invoice — it hasn't been issued yet
            // Cannot pay an already fully PAID invoice
            if(invoice.getStatus() == InvoiceStatus.DRAFT){
                throw new IllegalArgumentException(
                        "Cannot allocate payment to a DRAFT invoice: "
                                + invoice.getInvoiceNumber());
            }
            if(invoice.getStatus() == InvoiceStatus.PAID){
                throw new IllegalArgumentException(
                        "Invoice is already fully paid: "
                                + invoice.getInvoiceNumber());
            }

            // Get total already allocated to this invoice from previous payments
            BigDecimal alreadyAllocated = paymentAllocationRepository.getTotalAllocatedForInvoice(allocationReq.invoiceId());

            // Outstanding = grand total - already allocated
            BigDecimal outstanding = invoice.getGrandTotal().subtract(alreadyAllocated);

            // Outstanding = grand total - already allocated
            if(allocationReq.allocatedAmount().compareTo(outstanding) > 0){
                throw new IllegalArgumentException(
                        "Allocation amount (" + allocationReq.allocatedAmount()
                                + ") exceeds outstanding balance ("
                                + outstanding + ") for invoice: "
                                + invoice.getInvoiceNumber());
            }

        }

        // Step 5 — Generate sequential payment number from PostgreSQL SEQUENCE
        String paymentNumber = generatePaymentNumber();

        // Step 6 — Build Payment entity
        Payment payment = Payment.builder()
                .stockist(stockist)
                .chemist(chemist)
                .paymentNumber(paymentNumber)
                .paymentDate(request.paymentDate())
                .amount(request.amount())
                .paymentMode(request.paymentMode())
                .referenceNumber(request.referenceNumber())
                .notes(request.notes())
                .build();


        // Step 7 — Build PaymentAllocation entities and link to payment
        for(PaymentAllocationRequest allocationReq : request.allocations()){
            Invoice invoice = invoiceRepository.findByIdWithDetails(allocationReq.invoiceId()).orElseThrow();

            PaymentAllocation allocation = PaymentAllocation.builder()
                    .payment(payment)
                    .invoice(invoice)
                    .allocatedAmount(allocationReq.allocatedAmount())
                    .build();

            payment.getAllocations().add(allocation);
        }

        // Step 8 — Save payment — cascade saves all allocations automatically
        Payment saved = paymentRepository.save(payment);

        // Step 9 — Update invoice statuses based on total payments received
        // We do this AFTER saving so the new allocation is included in the sum
        for(PaymentAllocationRequest allocationReq : request.allocations()){
            Invoice invoice = invoiceRepository.findByIdWithDetails(
                    allocationReq.invoiceId()).orElseThrow();

            // Get total allocated including the one we just saved
            BigDecimal totalAllocatedForInvoice = paymentAllocationRepository.getTotalAllocatedForInvoice(allocationReq.invoiceId());

            //Determine new invoice status

            // Determine new invoice status
            if (totalAllocatedForInvoice.compareTo(invoice.getGrandTotal()) >= 0) {
                // Fully paid
                invoice.setStatus(InvoiceStatus.PAID);
            } else {
                // Partially paid
                invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
            }

            invoiceRepository.save(invoice);

        }

        // Step 10 — Re-fetch with JOIN FETCH for complete response
        return paymentMapper.toDto(
                paymentRepository.findByIdWithDetails(saved.getId()).orElseThrow()
        );
    }

    // Generate sequential payment number
    private String generatePaymentNumber() {
        Long nextVal = paymentRepository.getNextSequenceValue();
        return String.format("PAY-%d-%06d", LocalDate.now().getYear(), nextVal);
    }

}
