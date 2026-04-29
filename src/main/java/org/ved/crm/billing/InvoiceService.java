package org.ved.crm.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.common.exception.ResourceNotFoundException;
import org.ved.crm.order.Order;
import org.ved.crm.order.OrderItem;
import org.ved.crm.order.OrderRepository;
import org.ved.crm.order.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final InvoiceMapper invoiceMapper;

    @Value("${vedpharm.company.state}")
    private String companyState;

    public List<InvoiceDto> getAllInvoices(){
        return invoiceRepository.findAllWithDetails()
                .stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    public InvoiceDto getInvoiceById(UUID id){
       return invoiceRepository.findByIdWithDetails(id)
                .map(invoiceMapper::toDto)
                .orElseThrow(()->new ResourceNotFoundException("Invoice","id",id));
    }

    @Transactional
    public InvoiceDto generateInvoice(UUID orderId){

        // Step 1 — Load order with all items and relationships
        Order order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(()->new ResourceNotFoundException("Order","id",orderId));

        // Step 2 — Only CONFIRMED orders can be invoiced
        if(order.getStatus()!= OrderStatus.CONFIRMED){
            throw new IllegalArgumentException("Only CONFIRMED orders can be invoiced");
        }

        // Step 3 — One order can only ever produce one invoice
        if(invoiceRepository.existsByOrderId(orderId)){
            throw new IllegalArgumentException("Invoice already exists for this order");
        }

        // Step 4 — Compare states to determine tax type
        // Same state → CGST+SGST, Different state → IGST

        String stockistState = order.getStockist().getState();
        TaxType taxType = companyState.equalsIgnoreCase(stockistState)
                ?TaxType.CGST_SGST
                :TaxType.IGST;

        // Step 5 — Get next sequential invoice number from PostgreSQL SEQUENCE
        String invoiceNumber = generateInvoiceNumber();

        // Step 6 — Process each order item, calculate taxes, build line items
        List<InvoiceLineItem> lineItems = new ArrayList<>();
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;
        BigDecimal totalCgst = BigDecimal.ZERO;
        BigDecimal totalSgst = BigDecimal.ZERO;
        BigDecimal totalIgst = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (OrderItem orderItem : order.getOrderItems()){

            // Gross = unitPrice × quantity
            BigDecimal grossAmount = orderItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);

            // Discount in rupees = gross × discountPct / 100
            BigDecimal discountAmount = grossAmount
                    .multiply(orderItem.getDiscountPct())
                    .divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP);
        }




    }


    // Calls PostgreSQL SEQUENCE — atomic, gapless, legally compliant
    // Format: VED-2026-000001
    private String generateInvoiceNumber() {
        Long nextVal = invoiceRepository.getNextSequenceValue();
        return String.format("VED-%d-%06d", LocalDate.now().getYear(), nextVal);
    }
}
