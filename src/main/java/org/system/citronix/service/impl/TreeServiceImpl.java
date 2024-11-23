package org.system.citronix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.system.citronix.dto.request.TreeRequest;
import org.system.citronix.dto.response.TreeResponse;
import org.system.citronix.entity.Field;
import org.system.citronix.entity.Tree;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.TreeMapper;
import org.system.citronix.repository.FieldRepository;
import org.system.citronix.repository.TreeRepository;
import org.system.citronix.service.TreeService;
import org.system.citronix.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TreeServiceImpl implements TreeService {
    private final TreeRepository treeRepository;
    private final FieldRepository fieldRepository;
    private final TreeMapper treeMapper;

    @Override
    public TreeResponse plantTree(TreeRequest request) {
        Field field = fieldRepository.findById(request.getFieldId())
                .orElseThrow(() -> new ResourceNotFoundException("Field not found with id: " + request.getFieldId()));

        ValidationUtil.validatePlantingDate(request.getPlantDate());
        ValidationUtil.validateTreeDensity(field, 1);

        Tree tree = treeMapper.toEntity(request);
        tree.setField(field);
        return treeMapper.toResponse(treeRepository.save(tree) , LocalDate.now());
    }

    @Override
    public TreeResponse updateTree(Long id, TreeRequest request) {
        Tree tree = treeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with id: " + id));

        ValidationUtil.validatePlantingDate(request.getPlantDate());

        treeMapper.updateTreeFromRequest(request, tree);
        return treeMapper.toResponse(treeRepository.save(tree) , LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public TreeResponse getTreeById(Long id) {
        return treeRepository.findById(id)
                .map(tree -> treeMapper.toResponse(tree , LocalDate.now()))
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public TreeResponse getTreeWithHarvestDetails(Long id) {
        Tree tree = treeRepository.findByIdWithHarvestDetails(id);
        if (tree == null) {
            throw new ResourceNotFoundException("Tree not found with id: " + id);
        }
        return treeMapper.toResponse(tree , LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreeResponse> getAllTrees() {
        return treeRepository.findAll().stream()
                .map(tree -> treeMapper.toResponse(tree , LocalDate.now()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreeResponse> getTreesByFieldId(Long fieldId) {
        return treeRepository.findByFieldId(fieldId).stream()
                .map(tree -> treeMapper.toResponse(tree , LocalDate.now()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTree(Long id) {
        if (!treeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tree not found with id: " + id);
        }
        treeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreeResponse> getTreesByPlantingPeriod(LocalDate startDate, LocalDate endDate) {
        return treeRepository.findByPlantDateBetween(startDate, endDate).stream()
                .map(tree -> treeMapper.toResponse(tree , LocalDate.now()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TreeResponse> getTreesOlderThan(int age) {
        return treeRepository.findTreesOlderThan(age).stream()
                .map(tree -> treeMapper.toResponse(tree , LocalDate.now()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTreesInField(Long fieldId) {
        return treeRepository.countTreesByFieldId(fieldId);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateTreeProductivity(Long id) {
        Tree tree = treeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tree not found with id: " + id));
        return tree.getProductivity(LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public long countTreesPlantedInPeriod(Long fieldId, LocalDate startDate, LocalDate endDate) {
        return treeRepository.countTreesPlantedInPeriod(fieldId, startDate, endDate);
    }
}