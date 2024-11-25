package org.system.citronix.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.system.citronix.entity.*;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.exception.BusinessException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    void validateFarmArea_ValidArea_NoException() {
        assertDoesNotThrow(() -> ValidationUtil.validateFarmArea(100.0));
    }

    @Test
    void validateFarmArea_ZeroArea_ThrowsException() {
        assertThrows(BusinessException.class, () -> ValidationUtil.validateFarmArea(0.0));
    }

    @Test
    void validateFarmArea_NegativeArea_ThrowsException() {
        assertThrows(BusinessException.class, () -> ValidationUtil.validateFarmArea(-100.0));
    }

    @Test
    void validateFarmFields_ValidFields_NoException() {
        Farm farm = new Farm();
        farm.setArea(100.0);
        farm.setFields(new ArrayList<>());

        assertDoesNotThrow(() -> ValidationUtil.validateFarmFields(farm, 20.0));
    }

    @Test
    void validateFarmFields_TooManyFields_ThrowsException() {
        Farm farm = new Farm();
        farm.setArea(100.0);
        List<Field> fields = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            fields.add(new Field());
        }
        farm.setFields(fields);

        assertThrows(BusinessException.class, () -> ValidationUtil.validateFarmFields(farm, 20.0));
    }

    @Test
    void validateFieldArea_ValidArea_NoException() {
        assertDoesNotThrow(() -> ValidationUtil.validateFieldArea(0.1));
    }

    @Test
    void validateFieldArea_TooSmall_ThrowsException() {
        assertThrows(BusinessException.class, () -> ValidationUtil.validateFieldArea(0.05));
    }

    @ParameterizedTest
    @CsvSource({
            "100.0, 40.0, true",   // 40% of farm area
            "100.0, 51.0, false",  // 51% of farm area
            "50.0, 25.0, true",    // 50% of farm area
            "50.0, 30.0, false"    // 60% of farm area
    })
    void validateFieldToFarmRatio(Double farmArea, Double fieldArea, boolean shouldBeValid) {
        if (shouldBeValid) {
            assertDoesNotThrow(() -> ValidationUtil.validateFieldToFarmRatio(fieldArea, farmArea));
        } else {
            assertThrows(BusinessException.class, () -> ValidationUtil.validateFieldToFarmRatio(fieldArea, farmArea));
        }
    }

    @ParameterizedTest
    @CsvSource({
            "2024-03-15, true",  // March - valid
            "2024-04-15, true",  // April - valid
            "2024-05-15, true",  // May - valid
            "2024-06-15, false", // June - invalid
            "2024-02-15, false"  // February - invalid
    })
    void validatePlantingDate(LocalDate plantDate, boolean shouldBeValid) {
        if (shouldBeValid) {
            assertDoesNotThrow(() -> ValidationUtil.validatePlantingDate(plantDate));
        } else {
            assertThrows(BusinessException.class, () -> ValidationUtil.validatePlantingDate(plantDate));
        }
    }

    @Test
    void validateTreeDensity_ValidDensity_NoException() {
        Field field = new Field();
        field.setArea(1.0); // 1 hectare
        field.setTrees(new ArrayList<>());

        assertDoesNotThrow(() -> ValidationUtil.validateTreeDensity(field, 50)); // 50 trees < 100 max
    }

    @Test
    void validateTreeDensity_ExceedsDensity_ThrowsException() {
        Field field = new Field();
        field.setArea(1.0); // 1 hectare
        field.setTrees(new ArrayList<>());

        assertThrows(BusinessException.class, () -> ValidationUtil.validateTreeDensity(field, 101));
    }

    @Test
    void validateHarvestSeason_ValidSeason_NoException() {
        Harvest harvest = new Harvest();
        harvest.setSeason(SeasonEnum.WINTER);
        LocalDate winterDate = LocalDate.of(2024, 1, 15);

        assertDoesNotThrow(() -> ValidationUtil.validateHarvestSeason(harvest, winterDate));
    }

    @Test
    void validateHarvestSeason_InvalidSeason_ThrowsException() {
        Harvest harvest = new Harvest();
        harvest.setSeason(SeasonEnum.WINTER);
        LocalDate summerDate = LocalDate.of(2024, 7, 15);

        assertThrows(BusinessException.class, () -> ValidationUtil.validateHarvestSeason(harvest, summerDate));
    }

    @Test
    void validateSaleDate_ValidDate_NoException() {
        Sale sale = new Sale();
        sale.setDate(LocalDate.of(2024, 1, 20));
        Harvest harvest = new Harvest();
        harvest.setDate(LocalDate.of(2024, 1, 15));

        assertDoesNotThrow(() -> ValidationUtil.validateSaleDate(sale, harvest));
    }

    @Test
    void validateSaleDate_InvalidDate_ThrowsException() {
        Sale sale = new Sale();
        sale.setDate(LocalDate.of(2024, 1, 10));
        Harvest harvest = new Harvest();
        harvest.setDate(LocalDate.of(2024, 1, 15));

        assertThrows(BusinessException.class, () -> ValidationUtil.validateSaleDate(sale, harvest));
    }

    @Test
    void validateDateRange_ValidRange_NoException() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        assertDoesNotThrow(() -> ValidationUtil.validateDateRange(startDate, endDate));
    }

    @Test
    void validateDateRange_InvalidRange_ThrowsException() {
        LocalDate startDate = LocalDate.of(2024, 12, 31);
        LocalDate endDate = LocalDate.of(2024, 1, 1);

        assertThrows(BusinessException.class, () -> ValidationUtil.validateDateRange(startDate, endDate));
    }
}