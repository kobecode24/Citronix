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
class HarvestRepositoryTest {

    @Autowired
    private HarvestRepository harvestRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private TreeRepository treeRepository;

    private final LocalDate baseDate = LocalDate.of(2024, 1, 1); // Winter
    private final LocalDate springDate = LocalDate.of(2024, 4, 1); // Spring
    private final LocalDate summerDate = LocalDate.of(2024, 7, 1); // Summer
    private final LocalDate autumnDate = LocalDate.of(2024, 10, 1); // Autumn

    @BeforeEach
    void setUp() {
        harvestRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save harvest successfully")
    void shouldSaveHarvestSuccessfully() {
        Harvest harvest = Harvest.builder()
                .date(baseDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        Harvest savedHarvest = harvestRepository.save(harvest);

        assertThat(savedHarvest).isNotNull();
        assertThat(savedHarvest.getId()).isNotNull();
        assertThat(savedHarvest.getDate()).isEqualTo(baseDate);
        assertThat(savedHarvest.getSeason()).isEqualTo(SeasonEnum.WINTER);
    }

    @Test
    @DisplayName("Should find harvest by ID")
    void shouldFindHarvestById() {
        Harvest harvest = Harvest.builder()
                .date(baseDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        Harvest savedHarvest = harvestRepository.save(harvest);
        Optional<Harvest> foundHarvest = harvestRepository.findById(savedHarvest.getId());

        assertThat(foundHarvest).isPresent();
        assertThat(foundHarvest.get().getDate()).isEqualTo(baseDate);
        assertThat(foundHarvest.get().getSeason()).isEqualTo(SeasonEnum.WINTER);
    }

    @Test
    @DisplayName("Should find harvests by season")
    void shouldFindHarvestsBySeason() {
        List<Harvest> harvests = List.of(
                Harvest.builder()
                        .date(springDate)
                        .season(SeasonEnum.SPRING)
                        .totalQuantity(0.0)
                        .harvestDetails(new ArrayList<>())
                        .sales(null)
                        .build(),
                Harvest.builder()
                        .date(summerDate)
                        .season(SeasonEnum.SUMMER)
                        .totalQuantity(0.0)
                        .harvestDetails(new ArrayList<>())
                        .sales(null)
                        .build()
        );

        harvestRepository.saveAll(harvests);

        List<Harvest> springHarvests = harvestRepository.findBySeason(SeasonEnum.SPRING);

        assertThat(springHarvests).hasSize(1);
        assertThat(springHarvests.get(0).getDate()).isEqualTo(springDate);
    }

    @Test
    @DisplayName("Should find harvests by date range")
    void shouldFindHarvestsByDateRange() {
        List<Harvest> harvests = List.of(
                Harvest.builder()
                        .date(springDate)
                        .season(SeasonEnum.SPRING)
                        .totalQuantity(0.0)
                        .harvestDetails(new ArrayList<>())
                        .sales(null)
                        .build(),
                Harvest.builder()
                        .date(summerDate)
                        .season(SeasonEnum.SUMMER)
                        .totalQuantity(0.0)
                        .harvestDetails(new ArrayList<>())
                        .sales(null)
                        .build(),
                Harvest.builder()
                        .date(autumnDate)
                        .season(SeasonEnum.AUTUMN)
                        .totalQuantity(0.0)
                        .harvestDetails(new ArrayList<>())
                        .sales(null)
                        .build()
        );

        harvestRepository.saveAll(harvests);

        List<Harvest> harvestsInRange = harvestRepository.findByDateBetween(
                springDate,
                summerDate.plusDays(1)
        );

        assertThat(harvestsInRange).hasSize(2);
        assertThat(harvestsInRange)
                .extracting(Harvest::getSeason)
                .containsExactlyInAnyOrder(SeasonEnum.SPRING, SeasonEnum.SUMMER);
    }

    @Test
    @DisplayName("Should find harvest with details")
    void shouldFindHarvestWithDetails() {
        // Create and save a farm
        Farm farm = Farm.builder()
                .name("Test Farm")
                .location("Test Location")
                .area(10.0)
                .creationDate(baseDate)
                .fields(new ArrayList<>())
                .build();
        farm = farmRepository.save(farm);

        // Create and save a field
        Field field = Field.builder()
                .area(2.0)
                .farm(farm)
                .trees(new ArrayList<>())
                .build();
        field = fieldRepository.save(field);

        // Create and save a tree
        Tree tree = Tree.builder()
                .plantDate(baseDate)
                .field(field)
                .harvestDetails(new ArrayList<>())
                .build();
        tree = treeRepository.save(tree);

        // Create harvest with details
        Harvest harvest = Harvest.builder()
                .date(baseDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(10.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        HarvestDetail detail1 = HarvestDetail.builder()
                .harvest(harvest)
                .tree(tree)
                .quantity(5.0)
                .build();

        HarvestDetail detail2 = HarvestDetail.builder()
                .harvest(harvest)
                .tree(tree)
                .quantity(5.0)
                .build();

        harvest.getHarvestDetails().addAll(List.of(detail1, detail2));
        Harvest savedHarvest = harvestRepository.save(harvest);

        Optional<Harvest> harvestWithDetails = harvestRepository.findByIdWithDetails(savedHarvest.getId());

        assertThat(harvestWithDetails).isPresent();
        assertThat(harvestWithDetails.get().getHarvestDetails()).hasSize(2);
        assertThat(harvestWithDetails.get().getTotalQuantity()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should check if harvest exists by season and date")
    void shouldCheckIfHarvestExistsBySeasonAndDate() {
        Harvest harvest = Harvest.builder()
                .date(springDate)
                .season(SeasonEnum.SPRING)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        harvestRepository.save(harvest);

        boolean exists = harvestRepository.existsBySeasonAndDate(SeasonEnum.SPRING, springDate);
        boolean notExists = harvestRepository.existsBySeasonAndDate(SeasonEnum.SUMMER, springDate);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should sum total quantity between dates")
    void shouldSumTotalQuantityBetweenDates() {
        List<Harvest> harvests = List.of(
                Harvest.builder()
                        .date(springDate)
                        .season(SeasonEnum.SPRING)
                        .totalQuantity(10.0)
                        .harvestDetails(new ArrayList<>())
                        .sales(null)
                        .build(),
                Harvest.builder()
                        .date(summerDate)
                        .season(SeasonEnum.SUMMER)
                        .totalQuantity(20.0)
                        .harvestDetails(new ArrayList<>())
                        .sales(null)
                        .build()
        );

        harvestRepository.saveAll(harvests);

        Double totalQuantity = harvestRepository.sumTotalQuantityBetweenDates(
                springDate,
                summerDate.plusDays(1)
        );

        assertThat(totalQuantity).isEqualTo(30.0);
    }

    @Test
    @DisplayName("Should check if harvest exists by season and year")
    void shouldCheckIfHarvestExistsBySeasonAndYear() {
        Harvest harvest = Harvest.builder()
                .date(springDate)
                .season(SeasonEnum.SPRING)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        harvestRepository.save(harvest);

        boolean exists = harvestRepository.existsBySeasonAndYear(SeasonEnum.SPRING, 2024);
        boolean notExists = harvestRepository.existsBySeasonAndYear(SeasonEnum.SUMMER, 2024);

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should check if harvest exists by season and year excluding specific harvest")
    void shouldCheckIfHarvestExistsBySeasonAndYearAndIdNot() {
        Harvest harvest1 = Harvest.builder()
                .date(springDate)
                .season(SeasonEnum.SPRING)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        Harvest harvest2 = Harvest.builder()
                .date(springDate.plusYears(1))
                .season(SeasonEnum.SPRING)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        harvestRepository.saveAll(List.of(harvest1, harvest2));

        boolean exists = harvestRepository.existsBySeasonAndYearAndIdNot(
                SeasonEnum.SPRING,
                2024,
                harvest2.getId()
        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should delete harvest")
    void shouldDeleteHarvest() {
        Harvest harvest = Harvest.builder()
                .date(baseDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();

        Harvest savedHarvest = harvestRepository.save(harvest);
        harvestRepository.deleteById(savedHarvest.getId());

        Optional<Harvest> deletedHarvest = harvestRepository.findById(savedHarvest.getId());
        assertThat(deletedHarvest).isEmpty();
    }
}