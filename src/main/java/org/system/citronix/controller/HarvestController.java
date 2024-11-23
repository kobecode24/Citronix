package org.system.citronix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.system.citronix.dto.request.HarvestDetailRequest;
import org.system.citronix.dto.request.HarvestRequest;
import org.system.citronix.dto.response.HarvestDetailResponse;
import org.system.citronix.dto.response.HarvestResponse;
import org.system.citronix.dto.response.TreeHarvestValidationRequest;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.service.HarvestDetailService;
import org.system.citronix.service.HarvestService;
import org.system.citronix.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/harvests")
@RequiredArgsConstructor
@Tag(name = "Harvest Management", description = "Endpoints for managing harvests")
public class HarvestController {
    private final HarvestService harvestService;
    private final HarvestDetailService harvestDetailService;


    @PostMapping
    @Operation(summary = "Create a new harvest")
    public ResponseEntity<HarvestResponse> createHarvest(@Valid @RequestBody HarvestRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestService.createHarvest(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing harvest")
    public ResponseEntity<HarvestResponse> updateHarvest(
            @PathVariable Long id,
            @Valid @RequestBody HarvestRequest request
    ) {
        return ResponseEntity.ok(harvestService.updateHarvest(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get harvest by ID")
    public ResponseEntity<HarvestResponse> getHarvest(@PathVariable Long id) {
        return ResponseEntity.ok(harvestService.getHarvestById(id));
    }

    @GetMapping("/{id}/with-details")
    @Operation(summary = "Get harvest with its details")
    public ResponseEntity<HarvestResponse> getHarvestWithDetails(@PathVariable Long id) {
        return ResponseEntity.ok(harvestService.getHarvestWithDetails(id));
    }

    @GetMapping
    @Operation(summary = "Get all harvests")
    public ResponseEntity<List<HarvestResponse>> getAllHarvests() {
        return ResponseEntity.ok(harvestService.getAllHarvests());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a harvest")
    public ResponseEntity<Void> deleteHarvest(@PathVariable Long id) {
        harvestService.deleteHarvest(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-quantity/by-date-range")
    @Operation(summary = "Calculate total harvest quantity between dates")
    public ResponseEntity<Double> calculateTotalQuantityBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(harvestService.calculateTotalQuantityBetweenDates(startDate, endDate));
    }

    @PostMapping("/{harvestId}/details")
    @Operation(summary = "Add detail to harvest")
    public ResponseEntity<HarvestDetailResponse> addHarvestDetail(
            @PathVariable Long harvestId,
            @Valid @RequestBody HarvestDetailRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestDetailService.createHarvestDetail(harvestId, request));
    }

    @PutMapping("/details/{detailId}")
    @Operation(summary = "Update harvest detail")
    public ResponseEntity<HarvestDetailResponse> updateHarvestDetail(
            @PathVariable Long detailId,
            @Valid @RequestBody HarvestDetailRequest request
    ) {
        return ResponseEntity.ok(harvestDetailService.updateHarvestDetail(detailId, request));
    }

    @GetMapping("/details/{detailId}")
    @Operation(summary = "Get harvest detail by ID")
    public ResponseEntity<HarvestDetailResponse> getHarvestDetail(@PathVariable Long detailId) {
        return ResponseEntity.ok(harvestDetailService.getHarvestDetailById(detailId));
    }

    @GetMapping("/details")
    @Operation(summary = "Get all harvest details")
    public ResponseEntity<List<HarvestDetailResponse>> getAllHarvestDetails(){
        return ResponseEntity.ok(harvestDetailService.getAllHarvestDetails());
    }

    @GetMapping("/{harvestId}/details")
    @Operation(summary = "Get all details for a harvest")
    public ResponseEntity<List<HarvestDetailResponse>> getHarvestDetails(@PathVariable Long harvestId) {
        return ResponseEntity.ok(harvestDetailService.getHarvestDetailsByHarvestId(harvestId));
    }

    @DeleteMapping("/details/{detailId}")
    @Operation(summary = "Delete a harvest detail")
    public ResponseEntity<Void> deleteHarvestDetail(@PathVariable Long detailId) {
        harvestDetailService.deleteHarvestDetail(detailId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-season/{season}")
    @Operation(summary = "Get harvests by season")
    public ResponseEntity<List<HarvestResponse>> getHarvestsBySeason(@PathVariable SeasonEnum season) {
        return ResponseEntity.ok(harvestService.getHarvestsBySeason(season));
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Get harvests by date range")
    public ResponseEntity<List<HarvestResponse>> getHarvestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ValidationUtil.validateDateRange(startDate, endDate);
        return ResponseEntity.ok(harvestService.getHarvestsByDateRange(startDate, endDate));
    }

    @GetMapping("/details/by-tree/{treeId}")
    @Operation(summary = "Get harvest details by tree")
    public ResponseEntity<List<HarvestDetailResponse>> getHarvestDetailsByTree(@PathVariable Long treeId) {
        return ResponseEntity.ok(harvestDetailService.getHarvestDetailsByTreeId(treeId));
    }

    @PostMapping("/validate-tree-season")
    @Operation(summary = "Check if tree is already harvested in season")
    public boolean isTreeHarvestedInSeason(
            @Valid @RequestBody TreeHarvestValidationRequest request
    ) {
        return harvestDetailService.isTreeHarvestedInSeason(
                request.getTreeId(),
                request.getSeason(),
                request.getYear()
        );
    }

    @GetMapping("/{harvestId}/total-quantity")
    @Operation(summary = "Calculate total quantity for harvest")
    public ResponseEntity<Double> calculateTotalQuantityForHarvest(@PathVariable Long harvestId) {
        return ResponseEntity.ok(harvestDetailService.calculateTotalQuantityForHarvest(harvestId));
    }

    @PostMapping("/{harvestId}/details/by-field/{fieldId}")
    @Operation(summary = "Add harvest details for all trees in a field")
    public ResponseEntity<List<HarvestDetailResponse>> addHarvestDetailsByField(
            @PathVariable Long harvestId,
            @PathVariable Long fieldId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestDetailService.createHarvestDetailsForField(harvestId, fieldId));
    }

    @PostMapping("/{harvestId}/details/by-farm/{farmId}")
    @Operation(summary = "Add harvest details for all trees in a farm")
    public ResponseEntity<List<HarvestDetailResponse>> addHarvestDetailsByFarm(
            @PathVariable Long harvestId,
            @PathVariable Long farmId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(harvestDetailService.createHarvestDetailsForFarm(harvestId, farmId));
    }
}