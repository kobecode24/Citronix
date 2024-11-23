package org.system.citronix.service;

import org.system.citronix.dto.request.HarvestDetailRequest;
import org.system.citronix.dto.response.HarvestDetailResponse;
import org.system.citronix.enums.SeasonEnum;

import java.util.List;

public interface HarvestDetailService {
    HarvestDetailResponse createHarvestDetail(Long harvestId, HarvestDetailRequest request);
    HarvestDetailResponse updateHarvestDetail(Long id, HarvestDetailRequest request);
    HarvestDetailResponse getHarvestDetailById(Long id);
    List<HarvestDetailResponse> getAllHarvestDetails();
    List<HarvestDetailResponse> getHarvestDetailsByHarvestId(Long harvestId);
    List<HarvestDetailResponse> getHarvestDetailsByTreeId(Long treeId);
    void deleteHarvestDetail(Long id);
    Double calculateTotalQuantityForHarvest(Long harvestId);
    boolean isTreeHarvestedInSeason(Long treeId, SeasonEnum season, int year);
    List<HarvestDetailResponse> createHarvestDetailsForField(Long harvestId, Long fieldId);
    List<HarvestDetailResponse> createHarvestDetailsForFarm(Long harvestId, Long farmId);
}