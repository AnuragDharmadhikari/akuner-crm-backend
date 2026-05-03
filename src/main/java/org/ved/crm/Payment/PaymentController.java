package org.ved.crm.Payment;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ved.crm.common.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getAllPayments() {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully", paymentService.getAllPayments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentDto>> getPaymentById(@PathVariable UUID id){
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved successfully",paymentService.getPaymentById(id)));
    }

    @GetMapping("/stockist/{stockistId}")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPaymentsByStockist(@PathVariable UUID stockistId){
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully",paymentService.getPaymentsByStockist(stockistId)));
    }

    @GetMapping("/chemist/{chemistId}")
    public ResponseEntity<ApiResponse<List<PaymentDto>>> getPaymentsByChemist(@PathVariable UUID chemistId){
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved successfully",paymentService.getPaymentsByChemist(chemistId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentDto>> createPayment(@Valid @RequestBody CreatePaymentRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Payment created successfully",paymentService.createPayment(request)));
    }


}
