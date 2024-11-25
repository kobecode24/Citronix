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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FieldRepositoryTest {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private FarmRepository farmRepository;

    private Farm testFarm;
    private final LocalDate baseDate = LocalDate.of(2024, 1, 1);

    @BeforeEach
    void setUp() {
        // Create and save a test farm
        testFarm = Farm.builder()
                .name("Test Farm")
                .location("Test Location")
                .area(10.0)
                .creationDate(baseDate)
                .fields(new ArrayList<>())
                .build();
        testFarm = farmRepository.save(testFarm);
    }

    @Test
    @DisplayName("Should save field successfully")
    void shouldSaveFieldSuccessfully() {
        Field testField = Field.builder()
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        Field savedField = fieldRepository.save(testField);

        assertThat(savedField).isNotNull();
        assertThat(savedField.getId()).isNotNull();
        assertThat(savedField.getArea()).isEqualTo(testField.getArea());
        assertThat(savedField.getFarm().getId()).isEqualTo(testFarm.getId());
    }

    @Test
    @DisplayName("Should find field by ID")
    void shouldFindFieldById() {
        Field testField = Field.builder()
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        Field savedField = fieldRepository.save(testField);
        Optional<Field> foundField = fieldRepository.findById(savedField.getId());

        assertThat(foundField).isPresent();
        assertThat(foundField.get().getArea()).isEqualTo(testField.getArea());
    }

    @Test
    @DisplayName("Should find fields by farm ID")
    void shouldFindFieldsByFarmId() {
        // Create and save multiple fields for the same farm
        Field field1 = Field.builder()
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        Field field2 = Field.builder()
                .area(3.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        fieldRepository.saveAll(List.of(field1, field2));

        List<Field> fields = fieldRepository.findByFarmId(testFarm.getId());

        assertThat(fields).hasSize(2);
        assertThat(fields)
                .extracting(Field::getArea)
                .containsExactlyInAnyOrder(2.0, 3.0);
    }

    @Test
    @DisplayName("Should find fields by maximum area")
    void shouldFindFieldsByMaxArea() {
        // Create and save fields with different areas
        Field smallField = Field.builder()
                .area(1.5)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        Field largeField = Field.builder()
                .area(4.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        fieldRepository.saveAll(List.of(smallField, largeField));

        List<Field> fieldsUnder2Ha = fieldRepository.findByAreaLessThanEqual(2.0);

        assertThat(fieldsUnder2Ha).hasSize(1);
        assertThat(fieldsUnder2Ha.get(0).getArea()).isEqualTo(1.5);
    }

    @Test
    @DisplayName("Should count fields in farm")
    void shouldCountFieldsInFarm() {
        // Create and save multiple fields for the farm
        Field field1 = Field.builder()
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        Field field2 = Field.builder()
                .area(3.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        fieldRepository.saveAll(List.of(field1, field2));

        long count = fieldRepository.countFieldsByFarmId(testFarm.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Should find field with trees")
    void shouldFindFieldWithTrees() {
        // Create and save a field with trees
        Field field = Field.builder()
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        Tree tree1 = Tree.builder()
                .plantDate(baseDate)
                .field(field)
                .harvestDetails(new ArrayList<>())
                .build();

        Tree tree2 = Tree.builder()
                .plantDate(baseDate)
                .field(field)
                .harvestDetails(new ArrayList<>())
                .build();

        field.getTrees().addAll(List.of(tree1, tree2));
        Field savedField = fieldRepository.save(field);

        Field fieldWithTrees = fieldRepository.findByIdWithTrees(savedField.getId());

        assertThat(fieldWithTrees).isNotNull();
        assertThat(fieldWithTrees.getTrees()).hasSize(2);
    }

    @Test
    @DisplayName("Should calculate total area in farm")
    void shouldCalculateTotalAreaInFarm() {
        // Create and save multiple fields with different areas
        Field field1 = Field.builder()
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        Field field2 = Field.builder()
                .area(3.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        fieldRepository.saveAll(List.of(field1, field2));

        Double totalArea = fieldRepository.sumAreaByFarmId(testFarm.getId());

        assertThat(totalArea).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Should delete field")
    void shouldDeleteField() {
        Field testField = Field.builder()
                .area(2.0)
                .farm(testFarm)
                .trees(new ArrayList<>())
                .build();

        Field savedField = fieldRepository.save(testField);
        fieldRepository.deleteById(savedField.getId());

        Optional<Field> deletedField = fieldRepository.findById(savedField.getId());
        assertThat(deletedField).isEmpty();
    }
}