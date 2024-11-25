package org.system.citronix.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.system.citronix.entity.Farm;
import org.system.citronix.entity.Field;
import org.system.citronix.entity.Tree;
import org.system.citronix.entity.HarvestDetail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TreeRepositoryTest {

    @Autowired
    private TreeRepository treeRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private FarmRepository farmRepository;

    private Farm testFarm;
    private Field testField;
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
    }

    @Test
    @DisplayName("Should save tree successfully")
    void shouldSaveTreeSuccessfully() {
        Tree tree = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        Tree savedTree = treeRepository.save(tree);

        assertThat(savedTree).isNotNull();
        assertThat(savedTree.getId()).isNotNull();
        assertThat(savedTree.getPlantDate()).isEqualTo(baseDate);
        assertThat(savedTree.getField().getId()).isEqualTo(testField.getId());
    }

    @Test
    @DisplayName("Should find tree by ID")
    void shouldFindTreeById() {
        Tree tree = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        Tree savedTree = treeRepository.save(tree);
        Optional<Tree> foundTree = treeRepository.findById(savedTree.getId());

        assertThat(foundTree).isPresent();
        assertThat(foundTree.get().getPlantDate()).isEqualTo(baseDate);
    }

    @Test
    @DisplayName("Should find trees by field ID")
    void shouldFindTreesByFieldId() {
        // Create and save multiple trees
        Tree tree1 = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        Tree tree2 = Tree.builder()
                .plantDate(baseDate.plusDays(1))
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        treeRepository.saveAll(List.of(tree1, tree2));

        List<Tree> trees = treeRepository.findByFieldId(testField.getId());

        assertThat(trees).hasSize(2);
        assertThat(trees)
                .extracting(Tree::getPlantDate)
                .containsExactlyInAnyOrder(baseDate, baseDate.plusDays(1));
    }

    @Test
    @DisplayName("Should find trees by planting period")
    void shouldFindTreesByPlantingPeriod() {
        // Create trees with different planting dates
        Tree earlyTree = Tree.builder()
                .plantDate(baseDate.minusMonths(1))
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        Tree middleTree = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        Tree lateTree = Tree.builder()
                .plantDate(baseDate.plusMonths(1))
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        treeRepository.saveAll(List.of(earlyTree, middleTree, lateTree));

        List<Tree> treesInPeriod = treeRepository.findByPlantDateBetween(
                baseDate.minusDays(1),
                baseDate.plusDays(1)
        );

        assertThat(treesInPeriod).hasSize(1);
        assertThat(treesInPeriod.get(0).getPlantDate()).isEqualTo(baseDate);
    }

    @Test
    @DisplayName("Should count trees in field")
    void shouldCountTreesInField() {
        // Create and save multiple trees
        List<Tree> trees = List.of(
                Tree.builder().plantDate(baseDate).field(testField).harvestDetails(new ArrayList<>()).build(),
                Tree.builder().plantDate(baseDate.plusDays(1)).field(testField).harvestDetails(new ArrayList<>()).build()
        );
        treeRepository.saveAll(trees);

        long count = treeRepository.countTreesByFieldId(testField.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find tree with harvest details")
    void shouldFindTreeWithHarvestDetails() {
        // Create a tree with harvest details
        Tree tree = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        Tree savedTree = treeRepository.save(tree);

        Tree treeWithDetails = treeRepository.findByIdWithHarvestDetails(savedTree.getId());

        assertThat(treeWithDetails).isNotNull();
        assertThat(treeWithDetails.getHarvestDetails()).isNotNull();
    }

    @Test
    @DisplayName("Should count trees planted in period")
    void shouldCountTreesPlantedInPeriod() {
        // Create trees with different planting dates
        List<Tree> trees = List.of(
                Tree.builder().plantDate(baseDate).field(testField).harvestDetails(new ArrayList<>()).build(),
                Tree.builder().plantDate(baseDate.plusDays(5)).field(testField).harvestDetails(new ArrayList<>()).build(),
                Tree.builder().plantDate(baseDate.plusMonths(2)).field(testField).harvestDetails(new ArrayList<>()).build()
        );
        treeRepository.saveAll(trees);

        long count = treeRepository.countTreesPlantedInPeriod(
                testField.getId(),
                baseDate.minusDays(1),
                baseDate.plusDays(10)
        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find all trees by farm ID")
    void shouldFindAllTreesByFarmId() {
        // Create multiple fields and trees
        Field field2 = Field.builder()
                .area(1.5)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();
        field2 = fieldRepository.save(field2);

        List<Tree> treesField1 = List.of(
                Tree.builder().plantDate(baseDate).field(testField).harvestDetails(new ArrayList<>()).build(),
                Tree.builder().plantDate(baseDate.plusDays(1)).field(testField).harvestDetails(new ArrayList<>()).build()
        );

        List<Tree> treesField2 = List.of(
                Tree.builder().plantDate(baseDate).field(field2).harvestDetails(new ArrayList<>()).build()
        );

        treeRepository.saveAll(treesField1);
        treeRepository.saveAll(treesField2);

        List<Tree> farmTrees = treeRepository.findAllTreesByFarmId(testFarm.getId());

        assertThat(farmTrees).hasSize(3);
    }

    @Test
    @DisplayName("Should delete tree")
    void shouldDeleteTree() {
        Tree tree = Tree.builder()
                .plantDate(baseDate)
                .field(testField)
                .harvestDetails(new ArrayList<>())
                .build();

        Tree savedTree = treeRepository.save(tree);
        treeRepository.deleteById(savedTree.getId());

        Optional<Tree> deletedTree = treeRepository.findById(savedTree.getId());
        assertThat(deletedTree).isEmpty();
    }
}