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
class HarvestDetailRepositoryTest {

    @Autowired
    private HarvestDetailRepository harvestDetailRepository;

    @Autowired
    private HarvestRepository harvestRepository;

    @Autowired
    private FarmRepository farmRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private TreeRepository treeRepository;

    private Farm testFarm;
    private Field testField;
    private Tree testTree;
    private Harvest testHarvest;
    private final LocalDate baseDate = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        // Create and save farm
        testFarm = Farm.builder()
                .name("Test Farm")
                .location("Test Location")
                .area(10.0)
                .creationDate(baseDate)
                .fields(new ArrayList<>())
                .build();
        testFarm = farmRepository.save(testFarm);

        // Create and save field
        testField = Field.builder()
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();
        testField = fieldRepository.save(testField);

        // Create and save a tree
        testTree = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();
        testTree = treeRepository.save(testTree);

        // Create and save harvest
        testHarvest = Harvest.builder()
                .date(baseDate)
                .season(SeasonEnum.WINTER)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();
        testHarvest = harvestRepository.save(testHarvest);
    }

    @Test
    @DisplayName("Should save harvest detail successfully")
    void shouldSaveHarvestDetailSuccessfully() {
        HarvestDetail harvestDetail = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(10.0)
                .build();

        HarvestDetail savedDetail = harvestDetailRepository.save(harvestDetail);

        assertThat(savedDetail).isNotNull();
        assertThat(savedDetail.getId()).isNotNull();
        assertThat(savedDetail.getQuantity()).isEqualTo(10.0);
        assertThat(savedDetail.getHarvest().getId()).isEqualTo(testHarvest.getId());
        assertThat(savedDetail.getTree().getId()).isEqualTo(testTree.getId());
    }

    @Test
    @DisplayName("Should find harvest detail by ID")
    void shouldFindHarvestDetailById() {
        HarvestDetail harvestDetail = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(10.0)
                .build();

        HarvestDetail savedDetail = harvestDetailRepository.save(harvestDetail);
        Optional<HarvestDetail> foundDetail = harvestDetailRepository.findById(savedDetail.getId());

        assertThat(foundDetail).isPresent();
        assertThat(foundDetail.get().getQuantity()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should find harvest details by harvest ID")
    void shouldFindHarvestDetailsByHarvestId() {
        HarvestDetail detail1 = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(10.0)
                .build();

        // Create another tree for second detail
        Tree anotherTree = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();
        anotherTree = treeRepository.save(anotherTree);

        HarvestDetail detail2 = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(anotherTree)
                .quantity(15.0)
                .build();

        harvestDetailRepository.saveAll(List.of(detail1, detail2));

        List<HarvestDetail> details = harvestDetailRepository.findByHarvestId(testHarvest.getId());

        assertThat(details).hasSize(2);
        assertThat(details)
                .extracting(HarvestDetail::getQuantity)
                .containsExactlyInAnyOrder(10.0, 15.0);
    }

    @Test
    @DisplayName("Should find harvest details by tree ID")
    void shouldFindHarvestDetailsByTreeId() {
        // Create multiple harvests
        Harvest anotherHarvest = Harvest.builder()
                .date(baseDate.plusMonths(3))
                .season(SeasonEnum.SPRING)
                .totalQuantity(0.0)
                .harvestDetails(new ArrayList<>())
                .sales(null)
                .build();
        anotherHarvest = harvestRepository.save(anotherHarvest);

        HarvestDetail detail1 = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(10.0)
                .build();

        HarvestDetail detail2 = HarvestDetail.builder()
                .harvest(anotherHarvest)
                .tree(testTree)
                .quantity(15.0)
                .build();

        harvestDetailRepository.saveAll(List.of(detail1, detail2));

        List<HarvestDetail> details = harvestDetailRepository.findByTreeId(testTree.getId());

        assertThat(details).hasSize(2);
        assertThat(details)
                .extracting(HarvestDetail::getQuantity)
                .containsExactlyInAnyOrder(10.0, 15.0);
    }

    @Test
    @DisplayName("Should calculate total quantity for harvest")
    void shouldCalculateTotalQuantityForHarvest() {
        HarvestDetail detail1 = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(10.0)
                .build();

        // Create another tree for second detail
        Tree anotherTree = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();
        anotherTree = treeRepository.save(anotherTree);

        HarvestDetail detail2 = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(anotherTree)
                .quantity(15.0)
                .build();

        harvestDetailRepository.saveAll(List.of(detail1, detail2));

        Double totalQuantity = harvestDetailRepository.sumQuantityByHarvestId(testHarvest.getId());

        assertThat(totalQuantity).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Should check if tree is harvested in season")
    void shouldCheckIfTreeIsHarvestedInSeason() {
        HarvestDetail detail = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(10.0)
                .build();

        harvestDetailRepository.save(detail);

        boolean exists = harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(
                testTree.getId(),
                SeasonEnum.WINTER,
                baseDate.getYear()
        );

        boolean notExists = harvestDetailRepository.existsByTreeIdAndHarvestSeasonAndYear(
                testTree.getId(),
                SeasonEnum.SPRING,
                baseDate.getYear()
        );

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should delete harvest detail")
    void shouldDeleteHarvestDetail() {
        HarvestDetail detail = HarvestDetail.builder()
                .harvest(testHarvest)
                .tree(testTree)
                .quantity(10.0)
                .build();

        HarvestDetail savedDetail = harvestDetailRepository.save(detail);
        harvestDetailRepository.deleteById(savedDetail.getId());

        Optional<HarvestDetail> deletedDetail = harvestDetailRepository.findById(savedDetail.getId());
        assertThat(deletedDetail).isEmpty();
    }
}