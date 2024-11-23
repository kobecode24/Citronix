package org.system.citronix.service;

import org.system.citronix.dto.request.TreeRequest;
import org.system.citronix.dto.response.TreeResponse;

import java.time.LocalDate;
import java.util.List;

public interface TreeService {
    TreeResponse plantTree(TreeRequest request);
    TreeResponse updateTree(Long id, TreeRequest request);
    TreeResponse getTreeById(Long id);
    List<TreeResponse> getAllTrees();
    List<TreeResponse> getTreesByFieldId(Long fieldId);
    void deleteTree(Long id);
    List<TreeResponse> getTreesByPlantingPeriod(LocalDate startDate, LocalDate endDate);
    List<TreeResponse> getTreesOlderThan(int age);
    long countTreesInField(Long fieldId);
    double calculateTreeProductivity(Long id);
    long countTreesPlantedInPeriod(Long fieldId, LocalDate startDate, LocalDate endDate);
}