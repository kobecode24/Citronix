package org.system.citronix.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.system.citronix.dto.request.TreeRequest;
import org.system.citronix.dto.response.TreeResponse;
import org.system.citronix.entity.Farm;
import org.system.citronix.entity.Field;
import org.system.citronix.entity.Tree;
import org.system.citronix.exception.BusinessException;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.TreeMapper;
import org.system.citronix.repository.FieldRepository;
import org.system.citronix.repository.TreeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreeServiceImplTest {

    @Mock
    private TreeRepository treeRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private TreeMapper treeMapper;

    @InjectMocks
    private TreeServiceImpl treeService;

    private Farm testFarm;
    private Field testField;
    private Tree testTree;
    private TreeRequest testTreeRequest;
    private TreeResponse testTreeResponse;
    private final LocalDate baseDate = LocalDate.of(2024, 4, 1); // Valid planting month (April)

    @BeforeEach
    void setUp() {
        testFarm = Farm.builder()
                .id(1L)
                .name("Test Farm")
                .location("Test Location")
                .area(10.0)
                .creationDate(baseDate)
                .fields(new ArrayList<>())
                .build();

        testField = Field.builder()
                .id(1L)
                .area(1.0) // 1 hectare = max 100 trees
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        testTree = Tree.builder()
                .id(1L)
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        testTreeRequest = TreeRequest.builder()
                .plantDate(baseDate)
                .fieldId(1L)
                .build();

        testTreeResponse = TreeResponse.builder()
                .id(1L)
                .plantDate(baseDate)
                .fieldId(1L)
                .age(0)
                .productivity(2.5) // Young tree productivity
                .harvestDetails(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should plant tree successfully")
    void shouldPlantTreeSuccessfully() {
        when(fieldRepository.findById(anyLong())).thenReturn(Optional.of(testField));
        when(treeMapper.toEntity(any(TreeRequest.class))).thenReturn(testTree);
        when(treeRepository.save(any(Tree.class))).thenReturn(testTree);
        when(treeMapper.toResponse(any(Tree.class), any(LocalDate.class))).thenReturn(testTreeResponse);

        TreeResponse response = treeService.plantTree(testTreeRequest);

        assertThat(response).isNotNull();
        assertThat(response.getPlantDate()).isEqualTo(testTreeRequest.getPlantDate());
        assertThat(response.getFieldId()).isEqualTo(testTreeRequest.getFieldId());

        verify(treeRepository).save(any(Tree.class));
        verify(treeMapper).toResponse(any(Tree.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should throw exception when planting tree in invalid month")
    void shouldThrowExceptionWhenPlantingTreeInInvalidMonth() {
        testTreeRequest.setPlantDate(LocalDate.of(2024, 1, 1)); // January - invalid month
        when(fieldRepository.findById(anyLong())).thenReturn(Optional.of(testField));

        assertThrows(BusinessException.class, () ->
                treeService.plantTree(testTreeRequest)
        );

        verify(treeRepository, never()).save(any(Tree.class));
    }

    @Test
    @DisplayName("Should throw exception when field is at maximum tree density")
    void shouldThrowExceptionWhenFieldIsAtMaximumTreeDensity() {
        // Add maximum number of trees to field (100 per hectare)
        for (int i = 0; i < 100; i++) {
            testField.getTrees().add(Tree.builder().build());
        }

        when(fieldRepository.findById(anyLong())).thenReturn(Optional.of(testField));

        assertThrows(BusinessException.class, () ->
                treeService.plantTree(testTreeRequest)
        );

        verify(treeRepository, never()).save(any(Tree.class));
    }

    @Test
    @DisplayName("Should update tree successfully")
    void shouldUpdateTreeSuccessfully() {
        when(treeRepository.findById(anyLong())).thenReturn(Optional.of(testTree));
        when(treeRepository.save(any(Tree.class))).thenReturn(testTree);
        when(treeMapper.toResponse(any(Tree.class), any(LocalDate.class))).thenReturn(testTreeResponse);

        TreeResponse response = treeService.updateTree(1L, testTreeRequest);

        assertThat(response).isNotNull();
        assertThat(response.getPlantDate()).isEqualTo(testTreeRequest.getPlantDate());

        verify(treeRepository).save(any(Tree.class));
        verify(treeMapper).toResponse(any(Tree.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should get tree by ID")
    void shouldGetTreeById() {
        when(treeRepository.findById(anyLong())).thenReturn(Optional.of(testTree));
        when(treeMapper.toResponse(any(Tree.class), any(LocalDate.class))).thenReturn(testTreeResponse);

        TreeResponse response = treeService.getTreeById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testTree.getId());
    }

    @Test
    @DisplayName("Should get all trees")
    void shouldGetAllTrees() {
        List<Tree> trees = Arrays.asList(testTree);
        when(treeRepository.findAll()).thenReturn(trees);
        when(treeMapper.toResponse(any(Tree.class), any(LocalDate.class))).thenReturn(testTreeResponse);

        List<TreeResponse> responses = treeService.getAllTrees();

        assertThat(responses).hasSize(1);
        verify(treeMapper).toResponse(any(Tree.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should get trees by field ID")
    void shouldGetTreesByFieldId() {
        List<Tree> trees = Arrays.asList(testTree);
        when(treeRepository.findByFieldId(anyLong())).thenReturn(trees);
        when(treeMapper.toResponse(any(Tree.class), any(LocalDate.class))).thenReturn(testTreeResponse);

        List<TreeResponse> responses = treeService.getTreesByFieldId(1L);

        assertThat(responses).hasSize(1);
        verify(treeMapper).toResponse(any(Tree.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("Should get trees by planting period")
    void shouldGetTreesByPlantingPeriod() {
        List<Tree> trees = Arrays.asList(testTree);
        when(treeRepository.findByPlantDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(trees);
        when(treeMapper.toResponse(any(Tree.class), any(LocalDate.class))).thenReturn(testTreeResponse);

        List<TreeResponse> responses = treeService.getTreesByPlantingPeriod(
                baseDate.minusDays(1),
                baseDate.plusDays(1)
        );

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should get trees older than specified age")
    void shouldGetTreesOlderThanAge() {
        List<Tree> trees = Arrays.asList(testTree);
        when(treeRepository.findTreesOlderThan(anyInt())).thenReturn(trees);
        when(treeMapper.toResponse(any(Tree.class), any(LocalDate.class))).thenReturn(testTreeResponse);

        List<TreeResponse> responses = treeService.getTreesOlderThan(3);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should count trees in field")
    void shouldCountTreesInField() {
        when(treeRepository.countTreesByFieldId(anyLong())).thenReturn(5L);

        long count = treeService.countTreesInField(1L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should calculate tree productivity")
    void shouldCalculateTreeProductivity() {
        when(treeRepository.findById(anyLong())).thenReturn(Optional.of(testTree));

        double productivity = treeService.calculateTreeProductivity(1L);

        assertThat(productivity).isEqualTo(2.5); // Young tree productivity
    }

    @Test
    @DisplayName("Should count trees planted in period")
    void shouldCountTreesPlantedInPeriod() {
        when(treeRepository.countTreesPlantedInPeriod(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(3L);

        long count = treeService.countTreesPlantedInPeriod(
                1L,
                baseDate.minusDays(1),
                baseDate.plusDays(1)
        );

        assertThat(count).isEqualTo(3L);
    }

    @Test
    @DisplayName("Should delete tree successfully")
    void shouldDeleteTreeSuccessfully() {
        when(treeRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(treeRepository).deleteById(anyLong());

        treeService.deleteTree(1L);

        verify(treeRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent tree")
    void shouldThrowExceptionWhenDeletingNonExistentTree() {
        when(treeRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                treeService.deleteTree(1L)
        );

        verify(treeRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should calculate correct productivity for different tree ages")
    void shouldCalculateCorrectProductivityForDifferentTreeAges() {
        // Young tree (< 3 years)
        Tree youngTree = Tree.builder()
                .id(1L)
                .plantDate(LocalDate.now().minusYears(2))
                .field(testField)
                .build();

        // Mature tree (3-10 years)
        Tree matureTree = Tree.builder()
                .id(2L)
                .plantDate(LocalDate.now().minusYears(5))
                .field(testField)
                .build();

        // Old tree (> 10 years)
        Tree oldTree = Tree.builder()
                .id(3L)
                .plantDate(LocalDate.now().minusYears(12))
                .field(testField)
                .build();

        when(treeRepository.findById(1L)).thenReturn(Optional.of(youngTree));
        when(treeRepository.findById(2L)).thenReturn(Optional.of(matureTree));
        when(treeRepository.findById(3L)).thenReturn(Optional.of(oldTree));

        assertThat(treeService.calculateTreeProductivity(1L)).isEqualTo(2.5);
        assertThat(treeService.calculateTreeProductivity(2L)).isEqualTo(12.0);
        assertThat(treeService.calculateTreeProductivity(3L)).isEqualTo(20.0);
    }
}