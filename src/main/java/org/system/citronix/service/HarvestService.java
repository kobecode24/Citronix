package org.system.citronix.service;

import org.system.citronix.dto.request.HarvestRequest;
import org.system.citronix.dto.response.HarvestResponse;
import org.system.citronix.enums.SeasonEnum;

import java.time.LocalDate;
import java.util.List;

public interface HarvestService {
    HarvestResponse createHarvest(HarvestRequest request);
    HarvestResponse updateHarvest(Long id, HarvestRequest request);
    HarvestResponse getHarvestById(Long id);
    HarvestResponse getHarvestWithDetails(Long id);
    List<HarvestResponse> getAllHarvests();
    List<HarvestResponse> getHarvestsBySeason(SeasonEnum season);
    void deleteHarvest(Long id);
    List<HarvestResponse> getHarvestsByDateRange(LocalDate startDate, LocalDate endDate);
    Double calculateTotalQuantityBetweenDates(LocalDate startDate, LocalDate endDate);
}