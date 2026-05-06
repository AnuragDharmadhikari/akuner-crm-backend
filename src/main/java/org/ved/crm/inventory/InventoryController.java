package org.ved.crm.inventory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ved.crm.common.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/batches/product/{productId}")
    public ResponseEntity<ApiResponse<List<BatchDto>>> getBatchesByProduct(@PathVariable UUID productId){
        return ResponseEntity.ok(ApiResponse.success("Batches retrieved successfully",inventoryService.getBatchesByProduct(productId)));
    }

    @GetMapping("/batches/near-expiry")
    public ResponseEntity<ApiResponse<List<BatchDto>>> getNearExpiryBatches() {
        return ResponseEntity.ok(
                ApiResponse.success("Near expiry batches retrieved successfully",
                        inventoryService.getNearExpiryBatches()));
    }

    @GetMapping("/batches/expired")
    public ResponseEntity<ApiResponse<List<BatchDto>>> getExpiredBatchesWithStock() {
        return ResponseEntity.ok(
                ApiResponse.success("Expired batches retrieved successfully",
                        inventoryService.getExpiredBatchesWithStock()));
    }

    @GetMapping("/batches/{id}")
    public ResponseEntity<ApiResponse<BatchDto>> getBatchById(@PathVariable UUID id){
        return ResponseEntity.ok(ApiResponse.success("Batch retrieved successfully",inventoryService.getBatchById(id)));
    }

    @GetMapping("/batches/{batchId}/movements")
    public ResponseEntity<ApiResponse<List<StockMovementDto>>> getMovementsByBatch(@PathVariable UUID batchId){
        return ResponseEntity.ok(
                ApiResponse.success("Movements retrieved successfully",
                        inventoryService.getMovementsByBatch(batchId)));
    }

    @PostMapping("/batches")
    public ResponseEntity<ApiResponse<BatchDto>> addBatch(
            @Valid @RequestBody AddBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Batch added successfully",
                        inventoryService.addBatch(request)));
    }

    @PatchMapping("/batches/{batchId}/adjust")
    public ResponseEntity<ApiResponse<BatchDto>> adjustStock(
            @PathVariable UUID batchId,
            @Valid @RequestBody AdjustStockRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Stock adjusted successfully",
                        inventoryService.adjustStock(batchId, request)));
    }

    @PatchMapping("/batches/{batchId}/writeoff")
    public ResponseEntity<ApiResponse<BatchDto>> writeOffExpiredBatch(
            @PathVariable UUID batchId) {
        return ResponseEntity.ok(
                ApiResponse.success("Batch written off successfully",
                        inventoryService.writeOffExpiredBatch(batchId)));
    }

}
