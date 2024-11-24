package org.system.citronix.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.system.citronix.dto.request.FieldRequest;
import org.system.citronix.dto.response.FieldResponse;
import org.system.citronix.entity.Farm;
import org.system.citronix.entity.Field;
import org.system.citronix.entity.Tree;
import org.system.citronix.exception.BusinessException;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.FieldMapper;
import org.system.citronix.repository.FarmRepository;
import org.system.citronix.repository.FieldRepository;

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
class FieldServiceImplTest {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private FieldMapper fieldMapper;

    @InjectMocks
    private FieldServiceImpl fieldService;

    private Farm testFarm;
    private Field testField;
    private FieldRequest testFieldRequest;
    private FieldResponse testFieldResponse;
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

        testField = Field.builder()
                .id(1L)
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        testFieldRequest = FieldRequest.builder()
                .area(2.0)
                .farmId(1L)
                .build();

        testFieldResponse = FieldResponse.builder()
                .id(1L)
                .area(2.0)
                .farmId(1L)
                .trees(new ArrayList<>())
                .maximumTreeCapacity(200) // 2 hectares * 100 trees per hectare
                .availableTreeSpaces(200)
                .build();
    }

    @Test
    @DisplayName("Should create field successfully")
    void shouldCreateFieldSuccessfully() {
        when(farmRepository.findById(anyLong())).thenReturn(Optional.of(testFarm));
        when(fieldMapper.toEntity(any(FieldRequest.class))).thenReturn(testField);
        when(fieldRepository.save(any(Field.class))).thenReturn(testField);
        when(fieldMapper.toResponse(any(Field.class))).thenReturn(testFieldResponse);

        FieldResponse response = fieldService.createField(testFieldRequest);

        assertThat(response).isNotNull();
        assertThat(response.getArea()).isEqualTo(testFieldRequest.getArea());
        assertThat(response.getFarmId()).isEqualTo(testFieldRequest.getFarmId());

        verify(fieldRepository).save(any(Field.class));
        verify(fieldMapper).toResponse(any(Field.class));
    }

    @Test
    @DisplayName("Should throw exception when creating field with invalid area")
    void shouldThrowExceptionWhenCreatingFieldWithInvalidArea() {
        testFieldRequest.setArea(0.05); // Less than minimum area
        when(farmRepository.findById(anyLong())).thenReturn(Optional.of(testFarm));

        assertThrows(BusinessException.class, () ->
                fieldService.createField(testFieldRequest)
        );

        verify(fieldRepository, never()).save(any(Field.class));
    }

    @Test
    @DisplayName("Should throw exception when field area exceeds farm ratio")
    void shouldThrowExceptionWhenFieldAreaExceedsFarmRatio() {
        testFieldRequest.setArea(6.0); // More than 50% of farm area
        when(farmRepository.findById(anyLong())).thenReturn(Optional.of(testFarm));

        assertThrows(BusinessException.class, () ->
                fieldService.createField(testFieldRequest)
        );

        verify(fieldRepository, never()).save(any(Field.class));
    }

    @Test
    @DisplayName("Should update field successfully")
    void shouldUpdateFieldSuccessfully() {
        when(fieldRepository.findById(anyLong())).thenReturn(Optional.of(testField));
        when(farmRepository.findById(anyLong())).thenReturn(Optional.of(testFarm));
        when(fieldRepository.save(any(Field.class))).thenReturn(testField);
        when(fieldMapper.toResponse(any(Field.class))).thenReturn(testFieldResponse);

        FieldResponse response = fieldService.updateField(1L, testFieldRequest);

        assertThat(response).isNotNull();
        assertThat(response.getArea()).isEqualTo(testFieldRequest.getArea());

        verify(fieldRepository).save(any(Field.class));
        verify(fieldMapper).toResponse(any(Field.class));
    }

    @Test
    @DisplayName("Should get field by ID successfully")
    void shouldGetFieldByIdSuccessfully() {
        when(fieldRepository.findById(anyLong())).thenReturn(Optional.of(testField));
        when(fieldMapper.toResponse(any(Field.class))).thenReturn(testFieldResponse);

        FieldResponse response = fieldService.getFieldById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testField.getId());

        verify(fieldMapper).toResponse(testField);
    }

    @Test
    @DisplayName("Should get field with trees successfully")
    void shouldGetFieldWithTreesSuccessfully() {
        Tree tree = Tree.builder()
                .id(1L)
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();
        testField.getTrees().add(tree);

        when(fieldRepository.findByIdWithTrees(anyLong())).thenReturn(testField);
        when(fieldMapper.toResponseWithTrees(any(Field.class))).thenReturn(testFieldResponse);

        FieldResponse response = fieldService.getFieldWithTrees(1L);

        assertThat(response).isNotNull();
        verify(fieldMapper).toResponseWithTrees(testField);
    }

    @Test
    @DisplayName("Should get all fields")
    void shouldGetAllFields() {
        List<Field> fields = Arrays.asList(testField);
        when(fieldRepository.findAll()).thenReturn(fields);
        when(fieldMapper.toResponse(any(Field.class))).thenReturn(testFieldResponse);

        List<FieldResponse> responses = fieldService.getAllFields();

        assertThat(responses).hasSize(1);
        verify(fieldMapper).toResponse(testField);
    }

    @Test
    @DisplayName("Should get fields by farm ID")
    void shouldGetFieldsByFarmId() {
        List<Field> fields = Arrays.asList(testField);
        when(fieldRepository.findByFarmId(anyLong())).thenReturn(fields);
        when(fieldMapper.toResponse(any(Field.class))).thenReturn(testFieldResponse);

        List<FieldResponse> responses = fieldService.getFieldsByFarmId(1L);

        assertThat(responses).hasSize(1);
        verify(fieldMapper).toResponse(testField);
    }

    @Test
    @DisplayName("Should get fields by maximum area")
    void shouldGetFieldsByMaxArea() {
        List<Field> fields = Arrays.asList(testField);
        when(fieldRepository.findByAreaLessThanEqual(anyDouble())).thenReturn(fields);
        when(fieldMapper.toResponse(any(Field.class))).thenReturn(testFieldResponse);

        List<FieldResponse> responses = fieldService.getFieldsByMaxArea(3.0);

        assertThat(responses).hasSize(1);
        verify(fieldMapper).toResponse(testField);
    }

    @Test
    @DisplayName("Should count fields in farm")
    void shouldCountFieldsInFarm() {
        when(fieldRepository.countFieldsByFarmId(anyLong())).thenReturn(5L);

        long count = fieldService.countFieldsInFarm(1L);

        assertThat(count).isEqualTo(5L);
        verify(fieldRepository).countFieldsByFarmId(1L);
    }

    @Test
    @DisplayName("Should calculate total area in farm")
    void shouldCalculateTotalAreaInFarm() {
        when(fieldRepository.sumAreaByFarmId(anyLong())).thenReturn(8.0);

        Double totalArea = fieldService.calculateTotalAreaInFarm(1L);

        assertThat(totalArea).isEqualTo(8.0);
        verify(fieldRepository).sumAreaByFarmId(1L);
    }

    @Test
    @DisplayName("Should delete field successfully")
    void shouldDeleteFieldSuccessfully() {
        when(fieldRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(fieldRepository).deleteById(anyLong());

        fieldService.deleteField(1L);

        verify(fieldRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent field")
    void shouldThrowExceptionWhenDeletingNonExistentField() {
        when(fieldRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                fieldService.deleteField(1L)
        );

        verify(fieldRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when updating field would exceed farm capacity")
    void shouldThrowExceptionWhenUpdatingFieldWouldExceedFarmCapacity() {
        Field existingField = Field.builder()
                .id(2L)
                .area(3.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();
        testFarm.getFields().add(existingField);

        testFieldRequest.setArea(8.0); // This would exceed the farm's capacity with existing field

        when(fieldRepository.findById(anyLong())).thenReturn(Optional.of(testField));
        when(farmRepository.findById(anyLong())).thenReturn(Optional.of(testFarm));

        assertThrows(BusinessException.class, () ->
                fieldService.updateField(1L, testFieldRequest)
        );

        verify(fieldRepository, never()).save(any(Field.class));
    }
}