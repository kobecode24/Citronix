package org.system.citronix.service;

import org.system.citronix.dto.request.FieldRequest;
import org.system.citronix.dto.response.FieldResponse;

import java.util.List;

public interface FieldService {
    FieldResponse createField(FieldRequest request);
    FieldResponse updateField(Long id, FieldRequest request);
    FieldResponse getFieldById(Long id);
    FieldResponse getFieldWithTrees(Long id);
    List<FieldResponse> getAllFields();
    List<FieldResponse> getFieldsByFarmId(Long farmId);
    void deleteField(Long id);
    List<FieldResponse> getFieldsByMaxArea(Double maxArea);
    long countFieldsInFarm(Long farmId);
    Double calculateTotalAreaInFarm(Long farmId);
}