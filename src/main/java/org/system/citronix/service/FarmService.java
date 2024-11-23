package org.system.citronix.service;

import org.system.citronix.dto.request.FarmRequest;
import org.system.citronix.dto.response.FarmResponse;

import java.time.LocalDate;
import java.util.List;

public interface FarmService {
    FarmResponse createFarm(FarmRequest request);
    FarmResponse updateFarm(Long id, FarmRequest request);
    FarmResponse getFarmById(Long id);
    FarmResponse getFarmWithFields(Long id);
    double calculateLeftAreaInFarm(Long id);
    List<FarmResponse> getAllFarms();
    void deleteFarm(Long id);
    List<FarmResponse> getFarmsByMinArea(Double minArea);
    List<FarmResponse> getFarmsByDateRange(LocalDate startDate, LocalDate endDate);
    boolean isFarmNameUnique(String name);
}