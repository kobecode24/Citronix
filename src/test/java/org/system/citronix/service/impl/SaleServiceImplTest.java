package org.system.citronix.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.system.citronix.dto.request.SaleRequest;
import org.system.citronix.dto.response.SaleResponse;
import org.system.citronix.entity.Harvest;
import org.system.citronix.entity.Sale;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.exception.BusinessException;
import org.system.citronix.exception.ResourceNotFoundException;
import org.system.citronix.mapper.SaleMapper;
import org.system.citronix.repository.HarvestRepository;
import org.system.citronix.repository.SaleRepository;

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
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private HarvestRepository harvestRepository;

    @Mock
    private SaleMapper saleMapper;

    @InjectMocks
    private SaleServiceImpl saleService;

    private Harvest testHarvest;
    private Sale testSale;
    private SaleRequest testSaleRequest;
    private SaleResponse testSaleResponse;
    private final LocalDate baseDate = LocalDate.of(2024, 1, 1);
    private final double testQuantity = 100.0;
    private final double testUnitPrice = 2.5;

    @BeforeEach
    void setUp() {
        testHarvest = Harvest.builder()
                .id(1L)
                .date(baseDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(testQuantity)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        testSale = Sale.builder()
                .id(1L)
                .date(baseDate.plusDays(1))
                .unitPrice(testUnitPrice)
                .customer("Test Customer")
                .harvest(testHarvest)
                .build();

        testSaleRequest = SaleRequest.builder()
                .date(baseDate.plusDays(1))
                .unitPrice(testUnitPrice)
                .customer("Test Customer")
                .harvestId(1L)
                .build();

        testSaleResponse = SaleResponse.builder()
                .id(1L)
                .date(baseDate.plusDays(1))
                .unitPrice(testUnitPrice)
                .customer("Test Customer")
                .harvestId(1L)
                .revenue(testQuantity * testUnitPrice)
                .build();
    }

    @Test
    @DisplayName("Should create sale successfully")
    void shouldCreateSaleSuccessfully() {
        // Mock the mapper first
        when(saleMapper.toEntity(testSaleRequest)).thenReturn(testSale);

        // Mock repository methods
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);
        when(saleRepository.findById(anyLong())).thenReturn(Optional.of(testSale)); // Add this line
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(testSaleResponse);

        SaleResponse response = saleService.createSale(testSaleRequest);

        assertThat(response).isNotNull();
        assertThat(response.getUnitPrice()).isEqualTo(testSaleRequest.getUnitPrice());
        assertThat(response.getCustomer()).isEqualTo(testSaleRequest.getCustomer());
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    @DisplayName("Should throw exception when creating sale for harvest with no quantity")
    void shouldThrowExceptionWhenCreatingForHarvestWithNoQuantity() {
        testHarvest.setTotalQuantity(0.0);
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));

        assertThrows(BusinessException.class, () ->
                saleService.createSale(testSaleRequest)
        );

        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    @DisplayName("Should throw exception when sale date is before harvest date")
    void shouldThrowExceptionWhenSaleDateBeforeHarvestDate() {
        // Create a sale with invalid date
        Sale invalidSale = Sale.builder()
                .id(1L)
                .date(baseDate.minusDays(1))
                .unitPrice(testUnitPrice)
                .customer("Test Customer")
                .harvest(testHarvest)
                .build();

        // Mock the mapper to return the invalid sale
        when(saleMapper.toEntity(any(SaleRequest.class))).thenReturn(invalidSale);
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));

        assertThrows(BusinessException.class, () ->
                saleService.createSale(testSaleRequest)
        );

        verify(saleRepository, never()).save(any(Sale.class));
    }

    @Test
    @DisplayName("Should update sale successfully")
    void shouldUpdateSaleSuccessfully() {
        when(saleRepository.findById(anyLong())).thenReturn(Optional.of(testSale));
        when(harvestRepository.findById(anyLong())).thenReturn(Optional.of(testHarvest));
        when(saleRepository.save(any(Sale.class))).thenReturn(testSale);
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(testSaleResponse);

        SaleResponse response = saleService.updateSale(1L, testSaleRequest);

        assertThat(response).isNotNull();
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    @DisplayName("Should get sale by ID")
    void shouldGetSaleById() {
        when(saleRepository.findById(anyLong())).thenReturn(Optional.of(testSale));
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(testSaleResponse);

        SaleResponse response = saleService.getSaleById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testSale.getId());
    }

    @Test
    @DisplayName("Should get sales by harvest ID")
    void shouldGetSalesByHarvestId() {
        List<Sale> sales = Arrays.asList(testSale);
        when(saleRepository.findByHarvestId(anyLong())).thenReturn(sales);
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(testSaleResponse);

        List<SaleResponse> responses = saleService.getSalesByHarvestId(1L);

        assertThat(responses).hasSize(1);
        verify(saleMapper).toResponse(testSale);
    }

    @Test
    @DisplayName("Should get sales by date range")
    void shouldGetSalesByDateRange() {
        List<Sale> sales = Arrays.asList(testSale);
        when(saleRepository.findByDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(sales);
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(testSaleResponse);

        List<SaleResponse> responses = saleService.getSalesByDateRange(
                baseDate,
                baseDate.plusDays(2)
        );

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should get sales by customer")
    void shouldGetSalesByCustomer() {
        List<Sale> sales = Arrays.asList(testSale);
        when(saleRepository.findByCustomer(anyString())).thenReturn(sales);
        when(saleMapper.toResponse(any(Sale.class))).thenReturn(testSaleResponse);

        List<SaleResponse> responses = saleService.getSalesByCustomer("Test Customer");

        assertThat(responses).hasSize(1);
        verify(saleMapper).toResponse(testSale);
    }

    @Test
    @DisplayName("Should calculate total revenue between dates")
    void shouldCalculateTotalRevenueBetweenDates() {
        double expectedRevenue = testQuantity * testUnitPrice;
        when(saleRepository.calculateTotalRevenueBetweenDates(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(expectedRevenue);

        Double totalRevenue = saleService.calculateTotalRevenueBetweenDates(
                baseDate,
                baseDate.plusDays(2)
        );

        assertThat(totalRevenue).isEqualTo(expectedRevenue);
    }

    @Test
    @DisplayName("Should calculate average unit price by season")
    void shouldCalculateAverageUnitPriceBySeason() {
        when(saleRepository.calculateAverageUnitPriceBySeason(any(SeasonEnum.class)))
                .thenReturn(testUnitPrice);

        Double averagePrice = saleService.calculateAverageUnitPriceBySeason(SeasonEnum.WINTER);

        assertThat(averagePrice).isEqualTo(testUnitPrice);
    }


    @Test
    @DisplayName("Should delete sale successfully")
    void shouldDeleteSaleSuccessfully() {
        when(saleRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(saleRepository).deleteById(anyLong());

        saleService.deleteSale(1L);

        verify(saleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent sale")
    void shouldThrowExceptionWhenDeletingNonExistentSale() {
        when(saleRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                saleService.deleteSale(1L)
        );

        verify(saleRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should validate date range for sales query")
    void shouldValidateDateRangeForSalesQuery() {
        LocalDate endDate = baseDate.minusDays(1); // End date before start date

        assertThrows(BusinessException.class, () ->
                saleService.getSalesByDateRange(baseDate, endDate)
        );
    }

    @Test
    @DisplayName("Should validate date range for revenue calculation")
    void shouldValidateDateRangeForRevenueCalculation() {
        LocalDate endDate = baseDate.minusDays(1); // End date before start date

        assertThrows(BusinessException.class, () ->
                saleService.calculateTotalRevenueBetweenDates(baseDate, endDate)
        );
    }
}