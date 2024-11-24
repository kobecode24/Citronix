package org.system.citronix.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.system.citronix.entity.Farm;
import org.system.citronix.entity.Field;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FarmRepositoryTest {

    @Autowired
    private FarmRepository farmRepository;

    private Farm testFarm;
    private final LocalDate baseDate = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        // Create a test farm
        testFarm = Farm.builder()
                .name("Test Farm")
                .location("Test Location")
                .area(10.0)
                .creationDate(baseDate)
                .fields(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should save farm successfully")
    void shouldSaveFarmSuccessfully() {
        Farm savedFarm = farmRepository.save(testFarm);

        assertThat(savedFarm).isNotNull();
        assertThat(savedFarm.getId()).isNotNull();
        assertThat(savedFarm.getName()).isEqualTo(testFarm.getName());
        assertThat(savedFarm.getLocation()).isEqualTo(testFarm.getLocation());
        assertThat(savedFarm.getArea()).isEqualTo(testFarm.getArea());
        assertThat(savedFarm.getCreationDate()).isEqualTo(testFarm.getCreationDate());
    }

    @Test
    @DisplayName("Should find farm by ID")
    void shouldFindFarmById() {
        Farm savedFarm = farmRepository.save(testFarm);
        Optional<Farm> foundFarm = farmRepository.findById(savedFarm.getId());

        assertThat(foundFarm).isPresent();
        assertThat(foundFarm.get().getName()).isEqualTo(testFarm.getName());
    }

    @Test
    @DisplayName("Should find farms by minimum area")
    void shouldFindFarmsByMinArea() {
        // Save multiple farms with different areas
        Farm smallFarm = Farm.builder()
                .name("Small Farm")
                .location("Location 1")
                .area(5.0)
                .creationDate(baseDate)
                .build();

        Farm largeFarm = Farm.builder()
                .name("Large Farm")
                .location("Location 2")
                .area(15.0)
                .creationDate(baseDate)
                .build();

        farmRepository.saveAll(List.of(smallFarm, largeFarm));

        List<Farm> farmsAbove10 = farmRepository.findByAreaGreaterThanEqual(10.0);

        assertThat(farmsAbove10).hasSize(1);
        assertThat(farmsAbove10.get(0).getName()).isEqualTo("Large Farm");
    }

    @Test
    @DisplayName("Should find farms by creation date range")
    void shouldFindFarmsByCreationDateRange() {
        Farm oldFarm = Farm.builder()
                .name("Old Farm")
                .location("Old Location")
                .area(10.0)
                .creationDate(baseDate.minusMonths(2))
                .build();

        Farm newFarm = Farm.builder()
                .name("New Farm")
                .location("New Location")
                .area(10.0)
                .creationDate(baseDate.plusMonths(2))
                .build();

        farmRepository.saveAll(List.of(oldFarm, newFarm));

        List<Farm> farmsInRange = farmRepository.findByCreationDateBetween(
                baseDate.minusMonths(1),
                baseDate.plusMonths(1)
        );

        assertThat(farmsInRange).isEmpty();
    }

    @Test
    @DisplayName("Should check if farm name exists")
    void shouldCheckIfFarmNameExists() {
        farmRepository.save(testFarm);

        boolean exists = farmRepository.existsByName("Test Farm");
        boolean notExists = farmRepository.existsByName("Non-existent Farm");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Should count farms larger than specified area")
    void shouldCountFarmsLargerThan() {
        Farm smallFarm = Farm.builder()
                .name("Small Farm")
                .location("Location 1")
                .area(5.0)
                .creationDate(baseDate)
                .build();

        Farm largeFarm = Farm.builder()
                .name("Large Farm")
                .location("Location 2")
                .area(15.0)
                .creationDate(baseDate)
                .build();

        farmRepository.saveAll(List.of(smallFarm, largeFarm));

        long count = farmRepository.countFarmsLargerThan(10.0);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should find farm with fields")
    void shouldFindFarmWithFields() {
        // Create fields
        Field field1 = Field.builder()
                .area(2.0)
                .trees(new ArrayList<>())
                .build();

        Field field2 = Field.builder()
                .area(3.0)
                .trees(new ArrayList<>())
                .build();

        // Add fields to farm
        testFarm.getFields().add(field1);
        testFarm.getFields().add(field2);

        // Set bidirectional relationship
        field1.setFarm(testFarm);
        field2.setFarm(testFarm);

        // Save farm with fields
        Farm savedFarm = farmRepository.save(testFarm);

        // Fetch farm with fields
        Farm farmWithFields = farmRepository.findByIdWithFields(savedFarm.getId());

        // Assertions
        assertThat(farmWithFields).isNotNull();
        assertThat(farmWithFields.getFields()).hasSize(2);
        assertThat(farmWithFields.getFields())
                .extracting(Field::getArea)
                .containsExactlyInAnyOrder(2.0, 3.0);
    }

    @Test
    @DisplayName("Should delete farm")
    void shouldDeleteFarm() {
        Farm savedFarm = farmRepository.save(testFarm);
        farmRepository.deleteById(savedFarm.getId());

        Optional<Farm> deletedFarm = farmRepository.findById(savedFarm.getId());
        assertThat(deletedFarm).isEmpty();
    }
}