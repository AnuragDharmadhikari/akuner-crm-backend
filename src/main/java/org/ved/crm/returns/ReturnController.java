package org.ved.crm.returns;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ved.crm.common.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    // GET all returns
    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnDto>>> getAllReturns() {
        return ResponseEntity.ok(
                ApiResponse.success("Returns retrieved successfully",
                        returnService.getAllReturns()));
    }

    // GET return by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnDto>> getReturnById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Return retrieved successfully",
                        returnService.getReturnById(id)));
    }

    // GET returns by chemist
    @GetMapping("/chemist/{chemistId}")
    public ResponseEntity<ApiResponse<List<ReturnDto>>> getReturnsByChemist(
            @PathVariable UUID chemistId) {
        return ResponseEntity.ok(
                ApiResponse.success("Returns retrieved successfully",
                        returnService.getReturnsByChemist(chemistId)));
    }

    // GET returns by stockist
    @GetMapping("/stockist/{stockistId}")
    public ResponseEntity<ApiResponse<List<ReturnDto>>> getReturnsByStockist(
            @PathVariable UUID stockistId) {
        return ResponseEntity.ok(
                ApiResponse.success("Returns retrieved successfully",
                        returnService.getReturnsByStockist(stockistId)));
    }

    // GET open credit notes by chemist
    @GetMapping("/credit-notes/chemist/{chemistId}")
    public ResponseEntity<ApiResponse<List<CreditNoteDto>>> getOpenCreditNotesByChemist(
            @PathVariable UUID chemistId) {
        return ResponseEntity.ok(
                ApiResponse.success("Credit notes retrieved successfully",
                        returnService.getOpenCreditNotesByChemist(chemistId)));
    }

    // GET open credit notes by stockist
    @GetMapping("/credit-notes/stockist/{stockistId}")
    public ResponseEntity<ApiResponse<List<CreditNoteDto>>> getOpenCreditNotesByStockist(
            @PathVariable UUID stockistId) {
        return ResponseEntity.ok(
                ApiResponse.success("Credit notes retrieved successfully",
                        returnService.getOpenCreditNotesByStockist(stockistId)));
    }

    // GET credit note by ID
    @GetMapping("/credit-notes/{id}")
    public ResponseEntity<ApiResponse<CreditNoteDto>> getCreditNoteById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Credit note retrieved successfully",
                        returnService.getCreditNoteById(id)));
    }

    // POST create return — logs return with PENDING status
    @PostMapping
    public ResponseEntity<ApiResponse<ReturnDto>> createReturn(
            @Valid @RequestBody CreateReturnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Return logged successfully",
                        returnService.createReturn(request)));
    }

    // POST process return — adjusts stock, raises credit note
    @PostMapping("/{id}/process")
    public ResponseEntity<ApiResponse<ReturnDto>> processReturn(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Return processed successfully",
                        returnService.processReturn(id)));
    }

    // POST reject return — marks as rejected, no stock or financial changes
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ReturnDto>> rejectReturn(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Return rejected successfully",
                        returnService.rejectReturn(id)));
    }

    // POST apply credit note to an invoice
    @PostMapping("/credit-notes/{creditNoteId}/apply/{invoiceId}")
    public ResponseEntity<ApiResponse<CreditNoteDto>> applyCreditNote(
            @PathVariable UUID creditNoteId,
            @PathVariable UUID invoiceId) {
        return ResponseEntity.ok(
                ApiResponse.success("Credit note applied successfully",
                        returnService.applyCreditNote(creditNoteId, invoiceId)));
    }
}