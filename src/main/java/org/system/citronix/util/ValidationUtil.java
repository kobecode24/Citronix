package org.system.citronix.util;

import org.system.citronix.constant.CitronixConstants;
import org.system.citronix.dto.request.HarvestRequest;
import org.system.citronix.entity.*;
import org.system.citronix.enums.SeasonEnum;
import org.system.citronix.exception.BusinessException;

import java.time.LocalDate;

public class ValidationUtil {

    public static void validateFarmArea(Double area) {
        if (area <= 0) {
            throw new BusinessException("Farm area must be positive");
        }
    }

    public static void validateFarmFields(Farm farm, Double newFieldArea) {
        if (farm.getFields().size() >= CitronixConstants.MAX_FIELDS_PER_FARM) {
            throw new BusinessException("Farm has reached maximum number of fields: " + CitronixConstants.MAX_FIELDS_PER_FARM);
        }

        double totalFieldArea = farm.getFields().stream()
                .mapToDouble(Field::getArea)
                .sum() + newFieldArea;

        if (totalFieldArea >= farm.getArea()) {
            throw new BusinessException("Total field area cannot exceed farm area");
        }
    }

    public static void validateFieldArea(Double area) {
        if (area < CitronixConstants.MIN_FIELD_AREA) {
            throw new BusinessException("Field area must be at least " + CitronixConstants.MIN_FIELD_AREA + " hectares");
        }
    }

    public static void validateFieldToFarmRatio(Double fieldArea, Double farmArea) {
        if (fieldArea > (farmArea * CitronixConstants.MAX_FIELD_PERCENTAGE)) {
            throw new BusinessException("Field area cannot exceed 50% of farm area");
        }
    }

    public static void validatePlantingDate(LocalDate plantDate) {
        int month = plantDate.getMonthValue();
        if (month < CitronixConstants.PLANTING_START_MONTH || month > CitronixConstants.PLANTING_END_MONTH) {
            throw new BusinessException("Trees can only be planted between March and May");
        }
    }

    public static void validateTreeDensity(Field field, int newTreeCount) {
        int maxTrees = (int) (field.getArea() * CitronixConstants.MAX_TREES_PER_HECTARE);
        int currentTrees = field.getTrees().size();

        if (currentTrees + newTreeCount > maxTrees) {
            throw new BusinessException("Maximum tree density exceeded. Maximum allowed: " + maxTrees + " trees");
        }
    }

    public static void validateHarvestSeason(Harvest harvest, LocalDate date) {
        if (!harvest.isValidSeason(date)) {
            throw new BusinessException("Invalid harvest date for the specified season");
        }
    }

    public static void validateSaleDate(Sale sale, Harvest harvest) {
        if (sale.getDate().isBefore(harvest.getDate())) {
            throw new BusinessException("Sale date cannot be before harvest date");
        }
    }

    public static void validateSaleQuantity(Harvest harvest) {
        if (harvest.getTotalQuantity() <= 0) {
            throw new BusinessException("Cannot create sale for harvest with no quantity");
        }
    }

    public static void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date must be before end date");
        }
    }

    public static void validateHarvestSeasonUniqueness(Boolean exists, SeasonEnum season, int year) {
        if (exists) {
            throw new BusinessException(String.format(
                    "A harvest already exists for season %s in year %d",
                    season,
                    year
            ));
        }
    }

    public static void validateHarvestSeasonMatch(SeasonEnum requestSeason, LocalDate date) {
        SeasonEnum expectedSeason = SeasonEnum.fromDate(date);
        if (requestSeason != expectedSeason) {
            throw new BusinessException(String.format(
                    "Invalid season %s for date %s. Expected season: %s",
                    requestSeason,
                    date,
                    expectedSeason
            ));
        }
    }

    public static void validateHarvestUpdateWithDetails(Harvest harvest, HarvestRequest request) {
        // Don't allow changing date/season if harvest has details
        if (!harvest.getDate().equals(request.getDate()) ||
                !harvest.getSeason().equals(request.getSeason())) {
            throw new BusinessException(
                    "Cannot change harvest date or season when harvest details exist"
            );
        }
    }

    public static void validateTreeHarvestUpdate(
            Long currentTreeId,
            Long newTreeId,
            boolean alreadyHarvested
    ) {
        if (!currentTreeId.equals(newTreeId) && alreadyHarvested) {
            throw new BusinessException("Tree already harvested in this season");
        }
    }

    public static void validateTreeHarvestInSeason(boolean alreadyHarvested) {
        if (alreadyHarvested) {
            throw new BusinessException("Tree has already been harvested this season");
        }
    }
}