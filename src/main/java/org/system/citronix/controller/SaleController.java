package org.system.citronix.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.system.citronix.dto.request.SaleRequest;
import org.system.citronix.dto.response.SaleResponse;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.service.SaleService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
@Tag(name = "Sale Management", description = "Endpoints for managing sales")
public class SaleController {
    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Create a new sale")
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(saleService.createSale(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing sale")
    public ResponseEntity<SaleResponse> updateSale(
            @PathVariable Long id,
            @Valid @RequestBody SaleRequest request
    ) {
        return ResponseEntity.ok(saleService.updateSale(id, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID")
    public ResponseEntity<SaleResponse> getSale(@PathVariable Long id) {
        return ResponseEntity.ok(saleService.getSaleById(id));
    }

    @GetMapping
    @Operation(summary = "Get all sales")
    public ResponseEntity<List<SaleResponse>> getAllSales() {
        return ResponseEntity.ok(saleService.getAllSales());
    }

    @GetMapping("/by-harvest/{harvestId}")
    @Operation(summary = "Get sales by harvest ID")
    public ResponseEntity<List<SaleResponse>> getSalesByHarvest(@PathVariable Long harvestId) {
        return ResponseEntity.ok(saleService.getSalesByHarvestId(harvestId));
    }

    @GetMapping("/by-date-range")
    @Operation(summary = "Get sales by date range")
    public ResponseEntity<List<SaleResponse>> getSalesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(saleService.getSalesByDateRange(startDate, endDate));
    }

    @GetMapping("/by-customer/{customer}")
    @Operation(summary = "Get sales by customer")
    public ResponseEntity<List<SaleResponse>> getSalesByCustomer(
            @PathVariable String customer
    ) {
        return ResponseEntity.ok(saleService.getSalesByCustomer(customer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a sale")
    public ResponseEntity<Void> deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total-revenue/by-date-range")
    @Operation(summary = "Calculate total revenue between dates")
    public ResponseEntity<Double> calculateTotalRevenueBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(saleService.calculateTotalRevenueBetweenDates(startDate, endDate));
    }

    @GetMapping("/average-price/by-season/{season}")
    @Operation(summary = "Calculate average unit price by season")
    public ResponseEntity<Double> calculateAverageUnitPriceBySeason(
            @PathVariable SeasonEnum season
    ) {
        return ResponseEntity.ok(saleService.calculateAverageUnitPriceBySeason(season));
    }
}