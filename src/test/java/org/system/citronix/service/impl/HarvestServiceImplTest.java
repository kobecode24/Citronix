package org.system.citronix.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.system.citronix.dto.request.HarvestRequest;
import org.system.citronix.dto.response.HarvestResponse;
import org.system.citronix.entity.Harvest;
import org.system.citronix.entity.HarvestDetail;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.exception.BusinessException;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.HarvestMapper;
import org.system.citronix.repository.HarvestRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HarvestServiceImplTest {

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private HarvestMapper harvestMapper;

    @InjectMocks
    private HarvestServiceImpl harvestService;

    private Harvest testHarvest;
    private HarvestRequest testHarvestRequest;
    private HarvestResponse testHarvestResponse;
    private final LocalDate winterDate = LocalDate.of(2024, 1, 1);
    private final LocalDate springDate = LocalDate.of(2024, 4, 1);
    private final LocalDate summerDate = LocalDate.of(2024, 7, 1);
    private final LocalDate autumnDate = LocalDate.of(2024, 10, 1);

    @BeforeEach
    void setUp() {
        testHarvest = Harvest.builder()
                .id(1L)
                .date(winterDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        testHarvestRequest = HarvestRequest.builder()
                .date(winterDate)
                .season(SeasonEnum.WINTER)
                .build();

        testHarvestResponse = HarvestResponse.builder()
                .id(1L)
                .date(winterDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();
    }

    @Test
    @DisplayName("Should create harvest successfully")
    void shouldCreateHarvestSuccessfully() {
        when(harvestRepository.existsBySeasonAndYear(any(SeasonEnum.class), anyInt())).thenReturn(false);
        when(harvestMapper.toEntity(any(HarvestRequest.class))).thenReturn(testHarvest);
        when(harvestRepository.save(any(Harvest.class))).thenReturn(testHarvest);
        when(harvestMapper.toResponse(any(Harvest.class))).thenReturn(testHarvestResponse);

        HarvestResponse response = harvestService.createHarvest(testHarvestRequest);

        assertThat(response).isNotNull();
        assertThat(response.getDate()).isEqualTo(testHarvestRequest.getDate());
        assertThat(response.getSeason()).isEqualTo(testHarvestRequest.getSeason());

        verify(harvestRepository).save(any(Harvest.class));
    }

    @Test
    @DisplayName("Should throw exception when creating harvest with existing season in year")
    void shouldThrowExceptionWhenCreatingHarvestWithExistingSeason() {
        when(harvestRepository.existsBySeasonAndYear(any(SeasonEnum.class), anyInt())).thenReturn(true);

        assertThrows(BusinessException.class, () ->
                harvestService.createHarvest(testHarvestRequest)
        );

        verify(harvestRepository, never()).save(any(Harvest.class));
    }

    @Test
    @DisplayName("Should throw exception when harvest date doesn't match season")
    void shouldThrowExceptionWhenHarvestDateDoesntMatchSeason() {
        testHarvestRequest.setDate(springDate); // Spring date
        testHarvestRequest.setSeason(SeasonEnum.WINTER); // Winter season

        assertThrows(BusinessException.class, () ->
                harvestService.createHarvest(testHarvestRequest)
        );

        verify(harvestRepository, never()).save(any(Harvest.class));
    }

    @Test
    @DisplayName("Should update harvest successfully")
    void shouldUpdateHarvestSuccessfully() {
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(harvestRepository.save(any(Harvest.class))).thenReturn(testHarvest);
        when(harvestMapper.toResponse(any(Harvest.class))).thenReturn(testHarvestResponse);

        HarvestResponse response = harvestService.updateHarvest(1L, testHarvestRequest);

        assertThat(response).isNotNull();
        assertThat(response.getDate()).isEqualTo(testHarvestRequest.getDate());

        verify(harvestRepository).save(any(Harvest.class));
    }

    @Test
    @DisplayName("Should throw exception when updating harvest with details changes season")
    void shouldThrowExceptionWhenUpdatingHarvestWithDetailsChangesSeason() {
        Harvest harvestWithDetails = testHarvest;
        harvestWithDetails.getHarvestDetails().add(HarvestDetail.builder().build());

        HarvestRequest updateRequest = HarvestRequest.builder()
                .date(springDate)
                .season(SeasonEnum.SPRING)
                .build();

        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(harvestWithDetails));

        assertThrows(BusinessException.class, () ->
                harvestService.updateHarvest(1L, updateRequest)
        );

        verify(harvestRepository, never()).save(any(Harvest.class));
    }

    @Test
    @DisplayName("Should get harvest by ID")
    void shouldGetHarvestById() {
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(harvestMapper.toResponse(any(Harvest.class))).thenReturn(testHarvestResponse);

        HarvestResponse response = harvestService.getHarvestById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testHarvest.getId());
    }

    @Test
    @DisplayName("Should get harvest with details")
    void shouldGetHarvestWithDetails() {
        when(harvestRepository.findByIdWithDetails(anyLong())).thenReturn(Optional.of(testHarvest));
        when(harvestMapper.toResponseWithDetails(any(Harvest.class))).thenReturn(testHarvestResponse);

        HarvestResponse response = harvestService.getHarvestWithDetails(1L);

        assertThat(response).isNotNull();
        verify(harvestMapper).toResponseWithDetails(testHarvest);
    }

    @Test
    @DisplayName("Should get all harvests")
    void shouldGetAllHarvests() {
        List<Harvest> harvests = Arrays.asList(testHarvest);
        when(harvestRepository.findAll()).thenReturn(harvests);
        when(harvestMapper.toResponse(any(Harvest.class))).thenReturn(testHarvestResponse);

        List<HarvestResponse> responses = harvestService.getAllHarvests();

        assertThat(responses).hasSize(1);
        verify(harvestMapper).toResponse(testHarvest);
    }

    @Test
    @DisplayName("Should get harvests by season")
    void shouldGetHarvestsBySeason() {
        List<Harvest> harvests = Arrays.asList(testHarvest);
        when(harvestRepository.findBySeason(any(SeasonEnum.class))).thenReturn(harvests);
        when(harvestMapper.toResponse(any(Harvest.class))).thenReturn(testHarvestResponse);

        List<HarvestResponse> responses = harvestService.getHarvestsBySeason(SeasonEnum.WINTER);

        assertThat(responses).hasSize(1);
        verify(harvestMapper).toResponse(testHarvest);
    }

    @Test
    @DisplayName("Should get harvests by date range")
    void shouldGetHarvestsByDateRange() {
        List<Harvest> harvests = Arrays.asList(testHarvest);
        when(harvestRepository.findByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(harvests);
        when(harvestMapper.toResponse(any(Harvest.class))).thenReturn(testHarvestResponse);

        List<HarvestResponse> responses = harvestService.getHarvestsByDateRange(
                winterDate.minusDays(1),
                winterDate.plusDays(1)
        );

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should calculate total quantity between dates")
    void shouldCalculateTotalQuantityBetweenDates() {
        when(harvestRepository.sumTotalQuantityBetweenDates(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(100.0);

        Double totalQuantity = harvestService.calculateTotalQuantityBetweenDates(
                winterDate,
                springDate
        );

        assertThat(totalQuantity).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should delete harvest successfully")
    void shouldDeleteHarvestSuccessfully() {
        when(harvestRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(harvestRepository).deleteById(anyLong());

        harvestService.deleteHarvest(1L);

        verify(harvestRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent harvest")
    void shouldThrowExceptionWhenUpdatingNonExistentHarvest() {
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                harvestService.updateHarvest(1L, testHarvestRequest)
        );

        verify(harvestRepository, never()).save(any(Harvest.class));
    }

    @Test
    @DisplayName("Should validate season matches date for all seasons")
    void shouldValidateSeasonMatchesDateForAllSeasons() {
        // Common mocks for all tests
        when(harvestRepository.existsBySeasonAndYear(any(SeasonEnum.class), anyInt())).thenReturn(false);
        when(harvestRepository.save(any(Harvest.class))).thenReturn(testHarvest);
        when(harvestMapper.toResponse(any(Harvest.class))).thenReturn(testHarvestResponse);

        // Test Winter
        Harvest winterHarvest = createTestHarvest(winterDate, SeasonEnum.WINTER);
        testHarvestRequest = createTestHarvestRequest(winterDate, SeasonEnum.WINTER);
        when(harvestMapper.toEntity(testHarvestRequest)).thenReturn(winterHarvest);
        assertDoesNotThrow(() -> harvestService.createHarvest(testHarvestRequest));

        // Test Spring
        Harvest springHarvest = createTestHarvest(springDate, SeasonEnum.SPRING);
        testHarvestRequest = createTestHarvestRequest(springDate, SeasonEnum.SPRING);
        when(harvestMapper.toEntity(testHarvestRequest)).thenReturn(springHarvest);
        assertDoesNotThrow(() -> harvestService.createHarvest(testHarvestRequest));

        // Test Summer
        Harvest summerHarvest = createTestHarvest(summerDate, SeasonEnum.SUMMER);
        testHarvestRequest = createTestHarvestRequest(summerDate, SeasonEnum.SUMMER);
        when(harvestMapper.toEntity(testHarvestRequest)).thenReturn(summerHarvest);
        assertDoesNotThrow(() -> harvestService.createHarvest(testHarvestRequest));

        // Test Autumn
        Harvest autumnHarvest = createTestHarvest(autumnDate, SeasonEnum.AUTUMN);
        testHarvestRequest = createTestHarvestRequest(autumnDate, SeasonEnum.AUTUMN);
        when(harvestMapper.toEntity(testHarvestRequest)).thenReturn(autumnHarvest);
        assertDoesNotThrow(() -> harvestService.createHarvest(testHarvestRequest));

        // Verify that the mapper was called for each season
        verify(harvestMapper, times(4)).toEntity(any(HarvestRequest.class));
    }

    // Helper method to create test harvests
    private Harvest createTestHarvest(LocalDate date, SeasonEnum season) {
        return Harvest.builder()
                .id(1L)
                .date(date)
                .season(season)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();
    }

    // Helper method to create test harvest requests
    private HarvestRequest createTestHarvestRequest(LocalDate date, SeasonEnum season) {
        return HarvestRequest.builder()
                .date(date)
                .season(season)
                .build();
    }
}