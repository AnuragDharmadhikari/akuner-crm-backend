package org.ved.crm.target;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ved.crm.common.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/targets")
@RequiredArgsConstructor
public class CallTargetController {

    private final CallTargetService callTargetService;

    @GetMapping("/rep/{repId}")
    public ResponseEntity<ApiResponse<List<CallTargetDto>>> getTargetsByRep(@PathVariable UUID repId) {
        return ResponseEntity.ok(ApiResponse.success("Targets retrieved successfully", callTargetService.getTargetsByRep(repId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CallTargetDto>> getTargetById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success("Target retrieved successfully",
                        callTargetService.getTargetById(id)));
    }

    @GetMapping("/rep/{repId}/month/{month}/year/{year}")
    public ResponseEntity<ApiResponse<CallTargetDto>> getTargetByRepAndMonth(
            @PathVariable UUID repId,
            @PathVariable Integer month,
            @PathVariable Integer year) {
        return ResponseEntity.ok(
                ApiResponse.success("Target retrieved successfully",
                        callTargetService.getTargetByRepAndMonth(repId, month, year)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CallTargetDto>> createTarget(
            @Valid @RequestBody CreateCallTargetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Target created successfully",
                        callTargetService.createTarget(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CallTargetDto>> updateTarget(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCallTargetRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Target updated successfully",
                        callTargetService.updateTarget(id, request)));
    }

}
