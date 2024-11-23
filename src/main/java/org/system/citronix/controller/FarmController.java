package org.system.citronix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.system.citronix.dto.request.FarmRequest;
import org.system.citronix.dto.response.FarmResponse;
import org.system.citronix.service.FarmService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/farms")
@RequiredArgsConstructor
@Tag(name = "Farm Management", description = "Endpoints for managing farms")
public class FarmController {
    private final FarmService farmService;

    @PostMapping
    @Operation(summary = "Create a new farm")
    public ResponseEntity<FarmResponse> createFarm(@Valid @RequestBody FarmRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(farmService.createFarm(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing farm")
    public ResponseEntity<FarmResponse> updateFarm(
            @PathVariable Long id,
            @Valid @RequestBody FarmRequest request
    ) {
        return ResponseEntity.ok(farmService.updateFarm(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get farm by ID")
    public ResponseEntity<FarmResponse> getFarm(@PathVariable Long id) {
        return ResponseEntity.ok(farmService.getFarmById(id));
    }

    @GetMapping("/{id}/with-fields")
    @Operation(summary = "Get farm with its fields")
    public ResponseEntity<FarmResponse> getFarmWithFields(@PathVariable Long id) {
        return ResponseEntity.ok(farmService.getFarmWithFields(id));
    }

    @GetMapping
    @Operation(summary = "Get all farms")
    public ResponseEntity<List<FarmResponse>> getAllFarms() {
        return ResponseEntity.ok(farmService.getAllFarms());
    }

    @GetMapping("/{id}/left-area")
    @Operation(summary = "Calculate left area in a farm")
    public ResponseEntity<Double> calculateLeftAreaInFarm(@PathVariable Long id) {
        return ResponseEntity.ok(farmService.calculateLeftAreaInFarm(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a farm")
    public ResponseEntity<Void> deleteFarm(@PathVariable Long id) {
        farmService.deleteFarm(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-min-area/{minArea}")
    @Operation(summary = "Get farms by minimum area")
    public ResponseEntity<List<FarmResponse>> getFarmsByMinArea(
            @PathVariable Double minArea
    ) {
        return ResponseEntity.ok(farmService.getFarmsByMinArea(minArea));
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Get farms by creation date range")
    public ResponseEntity<List<FarmResponse>> getFarmsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(farmService.getFarmsByDateRange(startDate, endDate));
    }
}