package org.system.citronix.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.system.citronix.dto.request.FarmRequest;
import org.system.citronix.dto.response.FarmResponse;
import org.system.citronix.entity.Farm;
import org.system.citronix.entity.Field;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.exception.ValidationException;
import org.system.citronix.mapper.FarmMapper;
import org.system.citronix.repository.FarmRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmServiceImplTest {

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private FarmMapper farmMapper;

    @InjectMocks
    private FarmServiceImpl farmService;

    private Farm testFarm;
    private FarmRequest testFarmRequest;
    private FarmResponse testFarmResponse;
    private final LocalDate baseDate = LocalDate.of(2024, 1, 1);

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

        testFarmRequest = FarmRequest.builder()
                .name("Test Farm")
                .location("Test Location")
                .area(10.0)
                .creationDate(baseDate)
                .build();

        testFarmResponse = FarmResponse.builder()
                .id(1L)
                .name("Test Farm")
                .location("Test Location")
                .area(10.0)
                .creationDate(baseDate)
                .fields(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create farm successfully")
    void shouldCreateFarmSuccessfully() {
        when(farmRepository.existsByName(anyString())).thenReturn(false);
        when(farmMapper.toEntity(any(FarmRequest.class))).thenReturn(testFarm);
        when(farmRepository.save(any(Farm.class))).thenReturn(testFarm);
        when(farmMapper.toResponse(any(Farm.class))).thenReturn(testFarmResponse);

        FarmResponse response = farmService.createFarm(testFarmRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(testFarmRequest.getName());
        assertThat(response.getArea()).isEqualTo(testFarmRequest.getArea());

        verify(farmRepository).save(any(Farm.class));
        verify(farmMapper).toResponse(any(Farm.class));
    }

    @Test
    @DisplayName("Should throw ValidationException when creating farm with existing name")
    void shouldThrowExceptionWhenCreatingFarmWithExistingName() {
        when(farmRepository.existsByName(anyString())).thenReturn(true);

        assertThrows(ValidationException.class, () ->
                farmService.createFarm(testFarmRequest)
        );

        verify(farmRepository, never()).save(any(Farm.class));
    }

    @Test
    @DisplayName("Should update farm successfully")
    void shouldUpdateFarmSuccessfully() {
        when(farmRepository.findById(anyLong())).thenReturn(Optional.of(testFarm));
        when(farmRepository.save(any(Farm.class))).thenReturn(testFarm);
        when(farmMapper.toResponse(any(Farm.class))).thenReturn(testFarmResponse);

        FarmResponse response = farmService.updateFarm(1L, testFarmRequest);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(testFarmRequest.getName());

        verify(farmRepository).save(any(Farm.class));
        verify(farmMapper).toResponse(any(Farm.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent farm")
    void shouldThrowExceptionWhenUpdatingNonExistentFarm() {
        when(farmRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                farmService.updateFarm(1L, testFarmRequest)
        );

        verify(farmRepository, never()).save(any(Farm.class));
    }

    @Test
    @DisplayName("Should get farm by ID successfully")
    void shouldGetFarmByIdSuccessfully() {
        when(farmRepository.findById(anyLong())).thenReturn(Optional.of(testFarm));
        when(farmMapper.toResponse(any(Farm.class))).thenReturn(testFarmResponse);

        FarmResponse response = farmService.getFarmById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testFarm.getId());

        verify(farmMapper).toResponse(testFarm);
    }

    @Test
    @DisplayName("Should get farm with fields successfully")
    void shouldGetFarmWithFieldsSuccessfully() {
        Field field = Field.builder()
                .id(1L)
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();
        testFarm.getFields().add(field);

        when(farmRepository.findByIdWithFields(anyLong())).thenReturn(testFarm);
        when(farmMapper.toResponseWithFields(any(Farm.class))).thenReturn(testFarmResponse);

        FarmResponse response = farmService.getFarmWithFields(1L);

        assertThat(response).isNotNull();
        verify(farmMapper).toResponseWithFields(testFarm);
    }

    @Test
    @DisplayName("Should calculate left area in farm")
    void shouldCalculateLeftAreaInFarm() {
        Field field = Field.builder()
                .id(1L)
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();
        testFarm.getFields().add(field);

        when(farmRepository.findByIdWithFields(anyLong())).thenReturn(testFarm);

        double leftArea = farmService.calculateLeftAreaInFarm(1L);

        assertThat(leftArea).isEqualTo(8.0); // 10.0 - 2.0
    }

    @Test
    @DisplayName("Should get all farms")
    void shouldGetAllFarms() {
        List<Farm> farms = Arrays.asList(testFarm);
        when(farmRepository.findAll()).thenReturn(farms);
        when(farmMapper.toResponse(any(Farm.class))).thenReturn(testFarmResponse);

        List<FarmResponse> responses = farmService.getAllFarms();

        assertThat(responses).hasSize(1);
        verify(farmMapper).toResponse(testFarm);
    }

    @Test
    @DisplayName("Should get farms by minimum area")
    void shouldGetFarmsByMinArea() {
        List<Farm> farms = Arrays.asList(testFarm);
        when(farmRepository.findByAreaGreaterThanEqual(anyDouble())).thenReturn(farms);
        when(farmMapper.toResponse(any(Farm.class))).thenReturn(testFarmResponse);

        List<FarmResponse> responses = farmService.getFarmsByMinArea(5.0);

        assertThat(responses).hasSize(1);
        verify(farmMapper).toResponse(testFarm);
    }

    @Test
    @DisplayName("Should get farms by date range")
    void shouldGetFarmsByDateRange() {
        List<Farm> farms = Arrays.asList(testFarm);
        when(farmRepository.findByCreationDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(farms);
        when(farmMapper.toResponse(any(Farm.class))).thenReturn(testFarmResponse);

        List<FarmResponse> responses = farmService.getFarmsByDateRange(
                baseDate.minusDays(1),
                baseDate.plusDays(1)
        );

        assertThat(responses).hasSize(1);
        verify(farmMapper).toResponse(testFarm);
    }

    @Test
    @DisplayName("Should check if farm name is unique")
    void shouldCheckIfFarmNameIsUnique() {
        when(farmRepository.existsByName(anyString())).thenReturn(false);

        boolean isUnique = farmService.isFarmNameUnique("Test Farm");

        assertThat(isUnique).isTrue();
        verify(farmRepository).existsByName("Test Farm");
    }

    @Test
    @DisplayName("Should delete farm successfully")
    void shouldDeleteFarmSuccessfully() {
        when(farmRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(farmRepository).deleteById(anyLong());

        farmService.deleteFarm(1L);

        verify(farmRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent farm")
    void shouldThrowExceptionWhenDeletingNonExistentFarm() {
        when(farmRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                farmService.deleteFarm(1L)
        );

        verify(farmRepository, never()).deleteById(anyLong());
    }
}