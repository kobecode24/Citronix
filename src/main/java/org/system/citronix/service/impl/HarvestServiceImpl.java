package org.system.citronix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.system.citronix.dto.request.HarvestRequest;
import org.system.citronix.dto.response.HarvestResponse;
import org.system.citronix.entity.Harvest;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.HarvestMapper;
import org.system.citronix.repository.HarvestRepository;
import org.system.citronix.service.HarvestService;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HarvestServiceImpl implements HarvestService {
    private final HarvestRepository harvestRepository;
    private final HarvestMapper harvestMapper;

    @Override
    public HarvestResponse createHarvest(HarvestRequest request) {
        // Validate season uniqueness
        ValidationUtil.validateHarvestSeasonUniqueness(
                harvestRepository.existsBySeasonAndYear(request.getSeason(), request.getDate().getYear()),
                request.getSeason(),
                request.getDate().getYear()
        );

        // Validate season matches date
        ValidationUtil.validateHarvestSeasonMatch(request.getSeason(), request.getDate());

        Harvest harvest = harvestMapper.toEntity(request);
        harvest.setTotalQuantity(0.0);

        return harvestMapper.toResponse(harvestRepository.save(harvest));
    }

    @Override
    public HarvestResponse updateHarvest(Long id, HarvestRequest request) {
        // Find existing harvest
        Harvest harvest = harvestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found with id: " + id));

        // If season is being changed, validate new season uniqueness
        if (!harvest.getSeason().equals(request.getSeason())) {
            ValidationUtil.validateHarvestSeasonUniqueness(
                    harvestRepository.existsBySeasonAndYearAndIdNot(
                            request.getSeason(),
                            request.getDate().getYear(),
                            id
                    ),
                    request.getSeason(),
                    request.getDate().getYear()
            );
        }

        // Validate season matches date
        ValidationUtil.validateHarvestSeasonMatch(request.getSeason(), request.getDate());

        // If harvest has details, validate update is allowed
        if (!harvest.getHarvestDetails().isEmpty()) {
            ValidationUtil.validateHarvestUpdateWithDetails(harvest, request);
        }

        // Update harvest
        harvestMapper.updateHarvestFromRequest(request, harvest);
        return harvestMapper.toResponse(harvestRepository.save(harvest));
    }

    @Override
    @Transactional(readOnly = true)
    public HarvestResponse getHarvestById(Long id) {
        return harvestRepository.findById(id)
                .map(harvestMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public HarvestResponse getHarvestWithDetails(Long id) {
        return harvestRepository.findByIdWithDetails(id)
                .map(harvestMapper::toResponseWithDetails)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestResponse> getAllHarvests() {
        return harvestRepository.findAllWithDetails().stream()
                .map(harvestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestResponse> getHarvestsBySeason(SeasonEnum season) {
        return harvestRepository.findBySeason(season).stream()
                .map(harvestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteHarvest(Long id) {
        if (!harvestRepository.existsById(id)) {
            throw new ResourceNotFoundException("Harvest not found with id: " + id);
        }
        harvestRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestResponse> getHarvestsByDateRange(LocalDate startDate, LocalDate endDate) {
        return harvestRepository.findByDateBetween(startDate, endDate).stream()
                .map(harvestMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTotalQuantityBetweenDates(LocalDate startDate, LocalDate endDate) {
        return harvestRepository.sumTotalQuantityBetweenDates(startDate, endDate);
    }
}