package org.ved.crm.chemist;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ved.crm.common.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chemists")
public class ChemistController {

    private final ChemistService chemistService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChemistDto>>>getAllChemists(){
        List<ChemistDto> chemists = chemistService.getAllChemists();
        return ResponseEntity.ok(ApiResponse.success("Chemists retrieved successfully",chemists));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChemistDto>> getChemistById(@PathVariable UUID id){
        ChemistDto chemist = chemistService.getChemistById(id);
        return ResponseEntity.ok(ApiResponse.success("Chemist retrieved successfully",chemist));
    }

    @GetMapping("/rep/{repId}")
    public ResponseEntity<ApiResponse<List<ChemistDto>>> getChemistsByRep(@PathVariable UUID repId){
        List<ChemistDto> chemists = chemistService.getChemistByRep(repId);
        return ResponseEntity.ok(ApiResponse.success("Chemists retrieved successfully",chemists));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChemistDto>> createChemist(@Valid @RequestBody CreateChemistRequest request){
        ChemistDto chemist = chemistService.createChemist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Chemist created successfully",chemist));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChemistDto>> updateChemist(@PathVariable UUID id, @Valid @RequestBody UpdateChemistRequest request){
        ChemistDto chemist = chemistService.updateChemist(id,request);
        return ResponseEntity.ok(ApiResponse.success("Chemist updated successfully",chemist));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateChemist(@PathVariable UUID id){
        chemistService.deactivateChemist(id);
        return ResponseEntity.ok(ApiResponse.success("Chemist deactivated successfully"));
    }
}
