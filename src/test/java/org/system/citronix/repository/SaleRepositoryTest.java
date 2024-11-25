package org.system.citronix.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.system.citronix.entity.*;
import org.system.citronix.enums.SeasonEnum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SaleRepositoryTest {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private HarvestRepository harvestRepository;

    private final LocalDate baseDate = LocalDate.of(2024, 1, 1);
    private final double testQuantity = 100.0;
    private final double testUnitPrice = 2.5;

    private Harvest createAndSaveHarvest(int dayOffset) {
        Harvest harvest = Harvest.builder()
                .date(baseDate.plusDays(dayOffset))
                .season(SeasonEnum.WINTER)
                .totalQuantity(testQuantity)
                .harvestDetails(new ArrayList<>())
                .build();
        return harvestRepository.save(harvest);
    }

    private Sale createSale(Harvest harvest, String customer, LocalDate date, double unitPrice) {
        return Sale.builder()
                .date(date)
                .unitPrice(unitPrice)
                .customer(customer)
                .harvest(harvest)
                .build();
    }

    @Test
    @DisplayName("Should save sale successfully")
    void shouldSaveSaleSuccessfully() {
        Harvest harvest = createAndSaveHarvest(0);
        Sale sale = createSale(harvest, "Test Customer", baseDate.plusDays(1), testUnitPrice);

        Sale savedSale = saleRepository.save(sale);

        assertThat(savedSale).isNotNull();
        assertThat(savedSale.getId()).isNotNull();
        assertThat(savedSale.getUnitPrice()).isEqualTo(testUnitPrice);
        assertThat(savedSale.getCustomer()).isEqualTo("Test Customer");
        assertThat(savedSale.getHarvest().getId()).isEqualTo(harvest.getId());
    }

    @Test
    @DisplayName("Should find sale by ID")
    void shouldFindSaleById() {
        Harvest harvest = createAndSaveHarvest(0);
        Sale sale = createSale(harvest, "Test Customer", baseDate.plusDays(1), testUnitPrice);
        Sale savedSale = saleRepository.save(sale);

        Optional<Sale> foundSale = saleRepository.findById(savedSale.getId());

        assertThat(foundSale).isPresent();
        assertThat(foundSale.get().getUnitPrice()).isEqualTo(testUnitPrice);
        assertThat(foundSale.get().getCustomer()).isEqualTo("Test Customer");
    }

    @Test
    @DisplayName("Should find sale by harvest ID")
    void shouldFindSaleByHarvestId() {
        Harvest harvest = createAndSaveHarvest(0);
        Sale sale = createSale(harvest, "Test Customer", baseDate.plusDays(1), testUnitPrice);
        saleRepository.save(sale);

        List<Sale> sales = saleRepository.findByHarvestId(harvest.getId());

        assertThat(sales).hasSize(1);
        assertThat(sales.get(0).getCustomer()).isEqualTo("Test Customer");
    }

    @Test
    @DisplayName("Should find sales by date range")
    void shouldFindSalesByDateRange() {
        // Create three different harvests
        Harvest harvest1 = createAndSaveHarvest(0);
        Harvest harvest2 = createAndSaveHarvest(10);
        Harvest harvest3 = createAndSaveHarvest(20);

        // Create sales for each harvest
        Sale sale1 = createSale(harvest1, "Customer 1", baseDate.plusDays(1), testUnitPrice);
        Sale sale2 = createSale(harvest2, "Customer 2", baseDate.plusDays(10), testUnitPrice);
        Sale sale3 = createSale(harvest3, "Customer 3", baseDate.plusDays(20), testUnitPrice);

        saleRepository.save(sale1);
        saleRepository.save(sale2);
        saleRepository.save(sale3);

        List<Sale> salesInRange = saleRepository.findByDateBetween(
                baseDate,
                baseDate.plusDays(15)
        );

        assertThat(salesInRange).hasSize(2);
        assertThat(salesInRange)
                .extracting(Sale::getCustomer)
                .containsExactlyInAnyOrder("Customer 1", "Customer 2");
    }

    @Test
    @DisplayName("Should find sales by customer")
    void shouldFindSalesByCustomer() {
        // Create different harvests for each sale
        Harvest harvest1 = createAndSaveHarvest(1);
        Harvest harvest2 = createAndSaveHarvest(2);
        Harvest harvest3 = createAndSaveHarvest(3);

        Sale sale1 = createSale(harvest1, "Regular Customer", baseDate.plusDays(1), testUnitPrice);
        Sale sale2 = createSale(harvest2, "Regular Customer", baseDate.plusDays(2), testUnitPrice);
        Sale sale3 = createSale(harvest3, "Different Customer", baseDate.plusDays(3), testUnitPrice);

        saleRepository.save(sale1);
        saleRepository.save(sale2);
        saleRepository.save(sale3);

        List<Sale> customerSales = saleRepository.findByCustomer("Regular Customer");

        assertThat(customerSales).hasSize(2);
        assertThat(customerSales)
                .extracting(Sale::getDate)
                .containsExactlyInAnyOrder(baseDate.plusDays(1), baseDate.plusDays(2));
    }

    @Test
    @DisplayName("Should calculate total revenue between dates")
    void shouldCalculateTotalRevenueBetweenDates() {
        // Create different harvests for each sale
        Harvest harvest1 = createAndSaveHarvest(1);
        Harvest harvest2 = createAndSaveHarvest(10);

        Sale sale1 = createSale(harvest1, "Customer 1", baseDate.plusDays(1), testUnitPrice);
        Sale sale2 = createSale(harvest2, "Customer 2", baseDate.plusDays(10), testUnitPrice * 2);

        saleRepository.save(sale1);
        saleRepository.save(sale2);

        Double totalRevenue = saleRepository.calculateTotalRevenueBetweenDates(
                baseDate,
                baseDate.plusDays(15)
        );

        // Expected revenue = (quantity * unitPrice) + (quantity * unitPrice * 2)
        double expectedRevenue = (testQuantity * testUnitPrice) + (testQuantity * testUnitPrice * 2);
        assertThat(totalRevenue).isEqualTo(expectedRevenue);
    }

    @Test
    @DisplayName("Should calculate average unit price by season")
    void shouldCalculateAverageUnitPriceBySeason() {
        // Create different harvests for each sale
        Harvest harvest1 = createAndSaveHarvest(1);
        Harvest harvest2 = createAndSaveHarvest(2);

        Sale sale1 = createSale(harvest1, "Customer 1", baseDate.plusDays(1), 2.0);
        Sale sale2 = createSale(harvest2, "Customer 2", baseDate.plusDays(2), 4.0);

        saleRepository.save(sale1);
        saleRepository.save(sale2);

        Double averagePrice = saleRepository.calculateAverageUnitPriceBySeason(SeasonEnum.WINTER);

        assertThat(averagePrice).isEqualTo(3.0); // Average of 2.0 and 4.0
    }

    @Test
    @DisplayName("Should delete sale")
    void shouldDeleteSale() {
        Harvest harvest = createAndSaveHarvest(0);
        Sale sale = createSale(harvest, "Test Customer", baseDate.plusDays(1), testUnitPrice);

        Sale savedSale = saleRepository.save(sale);
        saleRepository.deleteById(savedSale.getId());

        Optional<Sale> deletedSale = saleRepository.findById(savedSale.getId());
        assertThat(deletedSale).isEmpty();
    }
}