package org.system.citronix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.system.citronix.dto.request.FieldRequest;
import org.system.citronix.dto.response.FieldResponse;
import org.system.citronix.service.FieldService;

import java.util.List;

@RestController
@RequestMapping("/fields")
@RequiredArgsConstructor
@Tag(name = "Field Management", description = "Endpoints for managing fields")
public class FieldController {
    private final FieldService fieldService;

    @PostMapping
    @Operation(summary = "Create a new field")
    public ResponseEntity<FieldResponse> createField(@Valid @RequestBody FieldRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fieldService.createField(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing field")
    public ResponseEntity<FieldResponse> updateField(
            @PathVariable Long id,
            @Valid @RequestBody FieldRequest request
    ) {
        return ResponseEntity.ok(fieldService.updateField(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get field by ID")
    public ResponseEntity<FieldResponse> getField(@PathVariable Long id) {
        return ResponseEntity.ok(fieldService.getFieldById(id));
    }

    @GetMapping("/{id}/with-trees")
    @Operation(summary = "Get field with its trees")
    public ResponseEntity<FieldResponse> getFieldWithTrees(@PathVariable Long id) {
        return ResponseEntity.ok(fieldService.getFieldWithTrees(id));
    }

    @GetMapping
    @Operation(summary = "Get all fields")
    public ResponseEntity<List<FieldResponse>> getAllFields() {
        return ResponseEntity.ok(fieldService.getAllFields());
    }

    @GetMapping("/by-farm/{farmId}")
    @Operation(summary = "Get fields by farm ID")
    public ResponseEntity<List<FieldResponse>> getFieldsByFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(fieldService.getFieldsByFarmId(farmId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a field")
    public ResponseEntity<Void> deleteField(@PathVariable Long id) {
        fieldService.deleteField(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-max-area/{maxArea}")
    @Operation(summary = "Get fields by maximum area")
    public ResponseEntity<List<FieldResponse>> getFieldsByMaxArea(
            @PathVariable Double maxArea
    ) {
        return ResponseEntity.ok(fieldService.getFieldsByMaxArea(maxArea));
    }

    @GetMapping("/count/by-farm/{farmId}")
    @Operation(summary = "Count fields in a farm")
    public ResponseEntity<Long> countFieldsInFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(fieldService.countFieldsInFarm(farmId));
    }

    @GetMapping("/total-area/by-farm/{farmId}")
    @Operation(summary = "Calculate total area of fields in a farm")
    public ResponseEntity<Double> calculateTotalAreaInFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(fieldService.calculateTotalAreaInFarm(farmId));
    }
}