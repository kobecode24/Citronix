package org.system.citronix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.system.citronix.dto.request.FarmRequest;
import org.system.citronix.dto.response.FarmResponse;
import org.system.citronix.entity.Farm;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.exception.ValidationException;
import org.system.citronix.mapper.FarmMapper;
import org.system.citronix.repository.FarmRepository;
import org.system.citronix.service.FarmService;
import org.system.citronix.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmServiceImpl implements FarmService {
    private final FarmRepository farmRepository;
    private final FarmMapper farmMapper;

    @Override
    public FarmResponse createFarm(FarmRequest request) {
        ValidationUtil.validateFarmArea(request.getArea());
        if (!isFarmNameUnique(request.getName())){
            throw new ValidationException("The name already exists");
        }
        Farm farm = farmMapper.toEntity(request);
        return farmMapper.toResponse(farmRepository.save(farm));
    }

    @Override
    public FarmResponse updateFarm(Long id, FarmRequest request) {
        Farm farm = farmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + id));

        farmMapper.updateFarmFromRequest(request, farm);
        return farmMapper.toResponse(farmRepository.save(farm));
    }

    @Override
    @Transactional(readOnly = true)
    public FarmResponse getFarmById(Long id) {
        return farmRepository.findById(id)
                .map(farmMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Farm not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public FarmResponse getFarmWithFields(Long id) {
        Farm farm = farmRepository.findByIdWithFields(id);
        if (farm == null) {
            throw new ResourceNotFoundException("Farm not found with id: " + id);
        }
        return farmMapper.toResponseWithFields(farm);
    }

    @Override
    public double calculateLeftAreaInFarm(Long id) {
        Farm farm = farmRepository.findByIdWithFields(id);
        if (farm == null) {
            throw new ResourceNotFoundException("Farm not found with id: " + id);
        }
        return farm.calculateLeftArea();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmResponse> getAllFarms() {
        return farmRepository.findAll().stream()
                .map(farmMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFarm(Long id) {
        if (!farmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Farm not found with id: " + id);
        }
        farmRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmResponse> getFarmsByMinArea(Double minArea) {
        return farmRepository.findByAreaGreaterThanEqual(minArea).stream()
                .map(farmMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FarmResponse> getFarmsByDateRange(LocalDate startDate, LocalDate endDate) {
        return farmRepository.findByCreationDateBetween(startDate, endDate).stream()
                .map(farmMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFarmNameUnique(String name) {
        return !farmRepository.existsByName(name);
    }
}