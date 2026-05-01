package org.ved.crm.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ved.crm.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getAllOrders() {
        return ResponseEntity.ok(
                ApiResponse.success("Orders retrieved successfully",
                        orderService.getAllOrders()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Order retrieved successfully",
                        orderService.getOrderById(id)));
    }

    @GetMapping("/stockist/{stockistId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrdersByStockist(
            @PathVariable UUID stockistId) {
        return ResponseEntity.ok(
                ApiResponse.success("Orders retrieved successfully",
                        orderService.getOrdersByStockist(stockistId)));
    }

    @GetMapping("/rep/{repId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrdersByRep(
            @PathVariable UUID repId) {
        return ResponseEntity.ok(
                ApiResponse.success("Orders retrieved successfully",
                        orderService.getOrdersByRep(repId)));
    }

    @GetMapping("/chemist/{chemistId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrdersByChemist(
            @PathVariable UUID chemistId) {
        return ResponseEntity.ok(
                ApiResponse.success("Orders retrieved successfully",
                        orderService.getOrdersByChemist(chemistId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully",
                        orderService.createOrder(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Order updated successfully",
                        orderService.updateOrder(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(
                ApiResponse.success("Order status updated successfully",
                        orderService.updateOrderStatus(id, status)));
    }
}