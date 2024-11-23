package org.system.citronix.service;

import org.system.citronix.dto.request.SaleRequest;
import org.system.citronix.dto.response.SaleResponse;
import org.system.citronix.enums.SeasonEnum;

import java.time.LocalDate;
import java.util.List;

public interface SaleService {
    SaleResponse createSale(SaleRequest request);
    SaleResponse updateSale(Long id, SaleRequest request);
    SaleResponse getSaleById(Long id);
    List<SaleResponse> getAllSales();
    List<SaleResponse> getSalesByHarvestId(Long harvestId);
    List<SaleResponse> getSalesByDateRange(LocalDate startDate, LocalDate endDate);
    List<SaleResponse> getSalesByCustomer(String customer);
    void deleteSale(Long id);
    Double calculateTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate);
    Double calculateAverageUnitPriceBySeason(SeasonEnum season);
}