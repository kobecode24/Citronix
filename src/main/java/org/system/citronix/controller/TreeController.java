package org.system.citronix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.system.citronix.dto.request.TreeRequest;
import org.system.citronix.dto.response.TreeResponse;
import org.system.citronix.service.TreeService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/trees")
@RequiredArgsConstructor
@Tag(name = "Tree Management", description = "Endpoints for managing trees")
public class TreeController {
    private final TreeService treeService;

    @PostMapping
    @Operation(summary = "Plant a new tree")
    public ResponseEntity<TreeResponse> plantTree(@Valid @RequestBody TreeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(treeService.plantTree(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing tree")
    public ResponseEntity<TreeResponse> updateTree(
            @PathVariable Long id,
            @Valid @RequestBody TreeRequest request
    ) {
        return ResponseEntity.ok(treeService.updateTree(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tree by ID")
    public ResponseEntity<TreeResponse> getTree(@PathVariable Long id) {
        return ResponseEntity.ok(treeService.getTreeById(id));
    }

    @GetMapping
    @Operation(summary = "Get all trees")
    public ResponseEntity<List<TreeResponse>> getAllTrees() {
        return ResponseEntity.ok(treeService.getAllTrees());
    }

    @GetMapping("/by-field/{fieldId}")
    @Operation(summary = "Get trees by field ID")
    public ResponseEntity<List<TreeResponse>> getTreesByField(@PathVariable Long fieldId) {
        return ResponseEntity.ok(treeService.getTreesByFieldId(fieldId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tree")
    public ResponseEntity<Void> deleteTree(@PathVariable Long id) {
        treeService.deleteTree(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-planting-period")
    @Operation(summary = "Get trees by planting period")
    public ResponseEntity<List<TreeResponse>> getTreesByPlantingPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(treeService.getTreesByPlantingPeriod(startDate, endDate));
    }

    @GetMapping("/older-than/{age}")
    @Operation(summary = "Get trees older than specified age")
    public ResponseEntity<List<TreeResponse>> getTreesOlderThan(@PathVariable int age) {
        return ResponseEntity.ok(treeService.getTreesOlderThan(age));
    }

    @GetMapping("/count/by-field/{fieldId}")
    @Operation(summary = "Count trees in a field")
    public ResponseEntity<Long> countTreesInField(@PathVariable Long fieldId) {
        return ResponseEntity.ok(treeService.countTreesInField(fieldId));
    }

    @GetMapping("/{id}/productivity")
    @Operation(summary = "Calculate tree productivity")
    public ResponseEntity<Double> calculateTreeProductivity(@PathVariable Long id) {
        return ResponseEntity.ok(treeService.calculateTreeProductivity(id));
    }

    @GetMapping("{id}/count-trees-planted-in-period")
    @Operation(summary = "count trees planted in period")
    public ResponseEntity<Long> countTreesPlantedInPeriod(@PathVariable Long id,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ){
        return ResponseEntity.ok(treeService.countTreesPlantedInPeriod(id,startDate,endDate));
    }
}