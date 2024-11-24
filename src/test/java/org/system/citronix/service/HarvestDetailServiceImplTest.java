package org.system.citronix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.system.citronix.dto.request.HarvestDetailRequest;
import org.system.citronix.dto.response.HarvestDetailResponse;
import org.system.citronix.entity.*;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.exception.BusinessException;
import org.system.citronix.mapper.HarvestDetailMapper;
import org.system.citronix.repository.*;
import org.system.citronix.service.impl.HarvestDetailServiceImpl;

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
class HarvestDetailServiceImplTest {

    @Mock
    private HarvestDetailRepository harvestDetailRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private TreeRepository treeRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private HarvestDetailMapper harvestDetailMapper;

    @InjectMocks
    private HarvestDetailServiceImpl harvestDetailService;

    private Farm testFarm;
    private Field testField;
    private Tree testTree;
    private Harvest testHarvest;
    private HarvestDetail testHarvestDetail;
    private HarvestDetailRequest testHarvestDetailRequest;
    private HarvestDetailResponse testHarvestDetailResponse;
    private final LocalDate baseDate = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        testFarm = Farm.builder()
                .id(1L)
                .name("Test Farm")
                .fields(new ArrayList<>())
                .build();

        testField = Field.builder()
                .id(1L)
                .farm(testFarm)
                .area(1.0)
                .trees(new ArrayList<>())
                .build();

        testTree = Tree.builder()
                .id(1L)
                .field(testField)
                .plantDate(baseDate.minusYears(2))
                .harvestDetails(new ArrayList<>())
                .build();

        testHarvest = Harvest.builder()
                .id(1L)
                .date(baseDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        testHarvestDetail = HarvestDetail.builder()
                .id(1L)
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(2.5)
                .build();

        testHarvestDetailRequest = HarvestDetailRequest.builder()
                .treeId(1L)
                .build();

        testHarvestDetailResponse = HarvestDetailResponse.builder()
                .id(1L)
                .harvestId(1L)
                .treeId(1L)
                .quantity(2.5)
                .build();
    }

    @Test
    @DisplayName("Should create harvest detail successfully")
    void shouldCreateHarvestDetailSuccessfully() {
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(treeRepository.findById(anyLong())).thenReturn(Optional.of(testTree));
        when(harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(anyLong(), any(SeasonEnum.class), anyInt()))
                .thenReturn(false);
        when(harvestDetailRepository.save(any(HarvestDetail.class))).thenReturn(testHarvestDetail);
        when(harvestDetailMapper.toResponse(any(HarvestDetail.class))).thenReturn(testHarvestDetailResponse);

        HarvestDetailResponse response = harvestDetailService.createHarvestDetail(1L, testHarvestDetailRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTreeId()).isEqualTo(testHarvestDetailRequest.getTreeId());
        verify(harvestDetailRepository).save(any(HarvestDetail.class));
    }

    @Test
    @DisplayName("Should throw exception when creating detail for already harvested tree")
    void shouldThrowExceptionWhenCreatingDetailForAlreadyHarvestedTree() {
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(treeRepository.findById(anyLong())).thenReturn(Optional.of(testTree));
        when(harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(anyLong(), any(SeasonEnum.class), anyInt()))
                .thenReturn(true);

        assertThrows(BusinessException.class, () ->
                harvestDetailService.createHarvestDetail(1L, testHarvestDetailRequest)
        );

        verify(harvestDetailRepository, never()).save(any(HarvestDetail.class));
    }

    @Test
    @DisplayName("Should create harvest details for field successfully")
    void shouldCreateHarvestDetailsForFieldSuccessfully() {
        List<Tree> trees = Arrays.asList(testTree);
        testField.setTrees(trees);

        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(fieldRepository.findByIdWithTrees(anyLong())).thenReturn(testField);
        when(harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(anyLong(), any(SeasonEnum.class), anyInt()))
                .thenReturn(false);
        when(harvestDetailRepository.saveAll(anyList())).thenReturn(Arrays.asList(testHarvestDetail));
        when(harvestDetailMapper.toResponse(any(HarvestDetail.class))).thenReturn(testHarvestDetailResponse);

        List<HarvestDetailResponse> responses = harvestDetailService.createHarvestDetailsForField(1L, 1L);

        assertThat(responses).hasSize(1);
        verify(harvestDetailRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should create harvest details for farm successfully")
    void shouldCreateHarvestDetailsForFarmSuccessfully() {
        // Set up test farm with fields
        testField.setTrees(Arrays.asList(testTree));
        testFarm.setFields(Arrays.asList(testField));

        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(farmRepository.findByIdWithFields(anyLong())).thenReturn(testFarm);
        when(treeRepository.findAllTreesByFarmId(anyLong())).thenReturn(Arrays.asList(testTree));
        when(harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(
                anyLong(), any(SeasonEnum.class), anyInt())).thenReturn(false);
        when(harvestDetailRepository.saveAll(anyList())).thenReturn(Arrays.asList(testHarvestDetail));
        doReturn(testHarvestDetailResponse)
                .when(harvestDetailMapper)
                .toResponse(any(HarvestDetail.class));

        List<HarvestDetailResponse> responses = harvestDetailService.createHarvestDetailsForFarm(1L, 1L);

        assertThat(responses).hasSize(1);
        verify(harvestDetailRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should get harvest details by harvest ID")
    void shouldGetHarvestDetailsByHarvestId() {
        List<HarvestDetail> details = Arrays.asList(testHarvestDetail);
        when(harvestDetailRepository.findByHarvestId(anyLong())).thenReturn(details);
        when(harvestDetailMapper.toResponse(any(HarvestDetail.class))).thenReturn(testHarvestDetailResponse);

        List<HarvestDetailResponse> responses = harvestDetailService.getHarvestDetailsByHarvestId(1L);

        assertThat(responses).hasSize(1);
        verify(harvestDetailMapper).toResponse(testHarvestDetail);
    }

    @Test
    @DisplayName("Should get harvest details by tree ID")
    void shouldGetHarvestDetailsByTreeId() {
        List<HarvestDetail> details = Arrays.asList(testHarvestDetail);
        when(harvestDetailRepository.findByTreeId(anyLong())).thenReturn(details);
        when(harvestDetailMapper.toResponse(any(HarvestDetail.class))).thenReturn(testHarvestDetailResponse);

        List<HarvestDetailResponse> responses = harvestDetailService.getHarvestDetailsByTreeId(1L);

        assertThat(responses).hasSize(1);
        verify(harvestDetailMapper).toResponse(testHarvestDetail);
    }

    @Test
    @DisplayName("Should calculate total quantity for harvest")
    void shouldCalculateTotalQuantityForHarvest() {
        when(harvestDetailRepository.sumQuantityByHarvestId(anyLong())).thenReturn(10.0);

        Double totalQuantity = harvestDetailService.calculateTotalQuantityForHarvest(1L);

        assertThat(totalQuantity).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should check if tree is harvested in season")
    void shouldCheckIfTreeIsHarvestedInSeason() {
        when(treeRepository.findById(anyLong())).thenReturn(Optional.of(testTree));
        when(harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(anyLong(), any(SeasonEnum.class), anyInt()))
                .thenReturn(true);

        boolean isHarvested = harvestDetailService.isTreeHarvestedInSeason(1L, SeasonEnum.WINTER, 2024);

        assertThat(isHarvested).isTrue();
    }

    @Test
    @DisplayName("Should throw exception when field has no trees")
    void shouldThrowExceptionWhenFieldHasNoTrees() {
        testField.setTrees(new ArrayList<>());
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(fieldRepository.findByIdWithTrees(anyLong())).thenReturn(testField);

        assertThrows(BusinessException.class, () ->
                harvestDetailService.createHarvestDetailsForField(1L, 1L)
        );

        verify(harvestDetailRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should update harvest detail successfully")
    void shouldUpdateHarvestDetailSuccessfully() {
        // Mock the tree
        when(treeRepository.findById(anyLong())).thenReturn(Optional.of(testTree));

        // Mock finding the harvest detail
        when(harvestDetailRepository.findById(anyLong())).thenReturn(Optional.of(testHarvestDetail));

        // Mock saving the harvest detail
        when(harvestDetailRepository.save(any(HarvestDetail.class))).thenReturn(testHarvestDetail);

        // Use doReturn for mapper to avoid strict stubbing issues
        doReturn(testHarvestDetailResponse)
                .when(harvestDetailMapper)
                .toResponse(any(HarvestDetail.class));

        HarvestDetailResponse response = harvestDetailService.updateHarvestDetail(1L, testHarvestDetailRequest);

        assertThat(response).isNotNull();
        assertThat(response.getTreeId()).isEqualTo(testHarvestDetailRequest.getTreeId());
        verify(harvestDetailRepository).save(any(HarvestDetail.class));
    }

    @Test
    @DisplayName("Should delete harvest detail successfully")
    void shouldDeleteHarvestDetailSuccessfully() {
        // Mock finding the harvest detail with its harvest
        HarvestDetail detailWithHarvest = HarvestDetail.builder()
                .id(1L)
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(10.0)
                .build();

        when(harvestDetailRepository.findById(anyLong())).thenReturn(Optional.of(detailWithHarvest));
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        doNothing().when(harvestDetailRepository).deleteById(anyLong());

        harvestDetailService.deleteHarvestDetail(1L);

        verify(harvestDetailRepository).deleteById(1L);
        verify(harvestRepository).save(any(Harvest.class));
    }
}