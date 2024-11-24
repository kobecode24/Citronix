package org.system.citronix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.system.citronix.dto.request.FieldRequest;
import org.system.citronix.dto.response.FieldResponse;
import org.system.citronix.entity.Farm;
import org.system.citronix.entity.Field;
import org.system.citronix.exception.BusinessException;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.FieldMapper;
import org.system.citronix.repository.FarmRepository;
import org.system.citronix.repository.FieldRepository;
import org.system.citronix.service.FieldService;
import org.system.citronix.util.ValidationUtil;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FieldServiceImpl implements FieldService {
    private final FieldRepository fieldRepository;
    private final FarmRepository farmRepository;
    private final FieldMapper fieldMapper;

    @Override
    public FieldResponse createField(FieldRequest request) {
        Farm farm = farmRepository.findById(request.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + request.getFarmId()));

        ValidationUtil.validateFieldArea(request.getArea());
        ValidationUtil.validateFieldToFarmRatio(request.getArea(), farm.getArea());
        ValidationUtil.validateFarmFields(farm, request.getArea());

        Field field = fieldMapper.toEntity(request);
        field.setFarm(farm);
        return fieldMapper.toResponse(fieldRepository.save(field));
    }

    @Override
    public FieldResponse updateField(Long id, FieldRequest request) {
        // Find the existing field
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + id));

        // Find the target farm
        Farm targetFarm = farmRepository.findById(request.getFarmId())
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + request.getFarmId()));

        // Validate the new area
        ValidationUtil.validateFieldArea(request.getArea());
        ValidationUtil.validateFieldToFarmRatio(request.getArea(), targetFarm.getArea());

        // Calculate total area excluding current field
        double totalAreaExcludingCurrentField = targetFarm.getFields().stream()
                .filter(f -> !f.getId().equals(id))
                .mapToDouble(Field::getArea)
                .sum();

        // Check if new area would exceed the farm's capacity
        if (totalAreaExcludingCurrentField + request.getArea() > targetFarm.getArea()) {
            throw new BusinessException(String.format(
                    "Updated field area (%.1f) would exceed farm's capacity. Available area: %.1f",
                    request.getArea(),
                    targetFarm.getArea() - totalAreaExcludingCurrentField
            ));
        }

        // Update the field
        fieldMapper.updateFieldFromRequest(request, field);
        field.setFarm(targetFarm);


        return fieldMapper.toResponse(fieldRepository.save(field));
    }

    @Override
    @Transactional(readOnly = true)
    public FieldResponse getFieldById(Long id) {
        return fieldRepository.findById(id)
                .map(fieldMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public FieldResponse getFieldWithTrees(Long id) {
        Field field = fieldRepository.findByIdWithTrees(id);
        if (field == null) {
            throw new ResourceNotFoundException("Field not found with id: " + id);
        }
        return fieldMapper.toResponseWithTrees(field);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FieldResponse> getAllFields() {
        return fieldRepository.findAll().stream()
                .map(fieldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FieldResponse> getFieldsByFarmId(Long farmId) {
        return fieldRepository.findByFarmId(farmId).stream()
                .map(fieldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteField(Long id) {
        if (!fieldRepository.existsById(id)) {
            throw new ResourceNotFoundException("Field not found with id: " + id);
        }
        fieldRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FieldResponse> getFieldsByMaxArea(Double maxArea) {
        return fieldRepository.findByAreaLessThanEqual(maxArea).stream()
                .map(fieldMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countFieldsInFarm(Long farmId) {
        return fieldRepository.countFieldsByFarmId(farmId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTotalAreaInFarm(Long farmId) {
        return fieldRepository.sumAreaByFarmId(farmId);
    }
}