package org.system.citronix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.system.citronix.dto.request.HarvestDetailRequest;
import org.system.citronix.dto.response.HarvestDetailResponse;
import org.system.citronix.entity.*;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.exception.BusinessException;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.HarvestDetailMapper;
import org.system.citronix.repository.*;
import org.system.citronix.service.HarvestDetailService;
import org.system.citronix.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HarvestDetailServiceImpl implements HarvestDetailService {
    private final HarvestDetailRepository harvestDetailRepository;
    private final HarvestRepository harvestRepository;
    private final TreeRepository treeRepository;
    private final HarvestDetailMapper harvestDetailMapper;
    private final FieldRepository fieldRepository;
    private final FarmRepository farmRepository;

    @Override
    public HarvestDetailResponse createHarvestDetail(Long harvestId, HarvestDetailRequest request) {
        Tree tree = treeRepository.findById(request.getTreeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found"));

        Harvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found"));

        // Validate tree hasn't been harvested this season
        ValidationUtil.validateTreeHarvestInSeason(
                harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(
                        request.getTreeId(),
                        harvest.getSeason(),
                        harvest.getDate().getYear()
                )
        );

        double quantity = tree.getProductivity(harvest.getDate());

        HarvestDetail detail = HarvestDetail.builder()
                .harvest(harvest)
                .tree(tree)
                .quantity(quantity)
                .build();

        return harvestDetailMapper.toResponse(
                updateHarvestDetailAndTotal(detail)
        );
    }

    private HarvestDetail updateHarvestDetailAndTotal(HarvestDetail detail) {
        HarvestDetail saved = harvestDetailRepository.save(detail);
        detail.getHarvest().calculateTotalQuantity();
        harvestRepository.save(detail.getHarvest());
        return saved;
    }

    @Override
    public HarvestDetailResponse updateHarvestDetail(Long id, HarvestDetailRequest request) {
        HarvestDetail harvestDetail = harvestDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest detail not found with id: " + id));

        Tree newTree = treeRepository.findById(request.getTreeId())
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with id: " + request.getTreeId()));

        ValidationUtil.validateTreeHarvestUpdate(
                harvestDetail.getTree().getId(),
                request.getTreeId(),
                harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(
                        request.getTreeId(),
                        harvestDetail.getHarvest().getSeason(),
                        harvestDetail.getHarvest().getDate().getYear()
                )
        );

        harvestDetail.setTree(newTree);
        harvestDetail.setQuantity(newTree.getProductivity(harvestDetail.getHarvest().getDate()));

        return harvestDetailMapper.toResponse(
                updateHarvestDetailAndTotal(harvestDetail)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public HarvestDetailResponse getHarvestDetailById(Long id) {
        return harvestDetailRepository.findById(id)
                .map(harvestDetailMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest detail not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestDetailResponse> getAllHarvestDetails() {
        return harvestDetailRepository.findAll().stream()
                .map(harvestDetailMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestDetailResponse> getHarvestDetailsByHarvestId(Long harvestId) {
        return harvestDetailRepository.findByHarvestId(harvestId).stream()
                .map(harvestDetailMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestDetailResponse> getHarvestDetailsByTreeId(Long treeId) {
        return harvestDetailRepository.findByTreeId(treeId).stream()
                .map(harvestDetailMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteHarvestDetail(Long id) {
        HarvestDetail harvestDetail = harvestDetailRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest detail not found with id: " + id));
        Long harvestId = harvestDetail.getHarvest().getId();

        harvestDetailRepository.deleteById(id);
        updateHarvestTotalQuantity(harvestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateTotalQuantityForHarvest(Long harvestId) {
        return harvestDetailRepository.sumQuantityByHarvestId(harvestId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTreeHarvestedInSeason(Long treeId, SeasonEnum season, int year) {
       treeRepository.findById(treeId)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found"));
        return harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(treeId, season, year);
    }

    @Override
    @Transactional
    public List<HarvestDetailResponse> createHarvestDetailsForField(Long harvestId, Long fieldId) {
        Harvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found"));

        Field field = fieldRepository.findByIdWithTrees(fieldId);
        if (field == null) {
            throw new ResourceNotFoundException("Field not found");
        }

        if (field.getTrees().isEmpty()) {
            throw new BusinessException("No trees found in field " + fieldId);
        }

        List<Tree> eligibleTrees = field.getTrees().stream()
                .filter(tree -> !harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(
                        tree.getId(),
                        harvest.getSeason(),
                        harvest.getDate().getYear()
                ))
                .filter(tree -> tree.getAge(LocalDate.now())>3)
                .toList();

        if (eligibleTrees.isEmpty()) {
            throw new BusinessException(
                    String.format("All trees in field %d have already been harvested in %s %d",
                            fieldId,
                            harvest.getSeason(),
                            harvest.getDate().getYear())
            );
        }

        List<HarvestDetail> details = eligibleTrees.stream()
                .map(tree -> HarvestDetail.builder()
                        .harvest(harvest)
                        .tree(tree)
                        .quantity(tree.getProductivity(harvest.getDate()))
                        .build())
                .collect(Collectors.toList());

        List<HarvestDetail> savedDetails = harvestDetailRepository.saveAll(details);

        // Update harvest total quantity
        harvest.calculateTotalQuantity();
        harvestRepository.save(harvest);

        return savedDetails.stream()
                .map(harvestDetailMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void updateHarvestTotalQuantity(Long harvestId) {
        Harvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found with id: " + harvestId));

        Double totalQuantity = calculateTotalQuantityForHarvest(harvestId);
        harvest.setTotalQuantity(totalQuantity != null ? totalQuantity : 0.0);
        harvestRepository.save(harvest);
    }

    @Override
    @Transactional
    public List<HarvestDetailResponse> createHarvestDetailsForFarm(Long harvestId, Long farmId) {
        Harvest harvest = harvestRepository.findById(harvestId)
                .orElseThrow(() -> new ResourceNotFoundException("Harvest not found"));

        Farm farm = farmRepository.findByIdWithFields(farmId);
        if (farm == null) {
            throw new ResourceNotFoundException("Farm not found");
        }

        if (farm.getFields().isEmpty()) {
            throw new BusinessException("No fields found in farm " + farmId);
        }

        List<Tree> allTrees = treeRepository.findAllTreesByFarmId(farmId);


        List<Tree> eligibleTrees = allTrees.stream()
                .filter(tree -> !harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(
                        tree.getId(),
                        harvest.getSeason(),
                        harvest.getDate().getYear()
                )).toList();

        if (eligibleTrees.isEmpty()) {
            throw new BusinessException(
                    String.format("All trees in farm %d have already been harvested in %s %d",
                            farmId,
                            harvest.getSeason(),
                            harvest.getDate().getYear())
            );
        }

        List<HarvestDetail> details = eligibleTrees.stream()
                .map(tree -> HarvestDetail.builder()
                        .harvest(harvest)
                        .tree(tree)
                        .quantity(tree.getProductivity(harvest.getDate()))
                        .build())
                .collect(Collectors.toList());

        List<HarvestDetail> savedDetails = harvestDetailRepository.saveAll(details);

        // Update harvest total quantity
        harvest.calculateTotalQuantity();
        harvestRepository.save(harvest);

        return savedDetails.stream()
                .map(harvestDetailMapper::toResponse)
                .collect(Collectors.toList());
    }
}