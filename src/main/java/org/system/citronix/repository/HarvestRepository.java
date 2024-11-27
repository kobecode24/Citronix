package org.system.citronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.system.citronix.entity.Harvest;
import org.system.citronix.enums.SeasonEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HarvestRepository extends JpaRepository<Harvest, Long> {
    List<Harvest> findBySeason(SeasonEnum season);

    @Query("SELECT h FROM Harvest h WHERE h.date BETWEEN :startDate AND :endDate")
    List<Harvest> findByDateBetween(LocalDate startDate, LocalDate endDate);


    @Query("SELECT DISTINCT h FROM Harvest h " +
            "LEFT JOIN FETCH h.harvestDetails hd " +
            "LEFT JOIN FETCH h.sales s " +
            "WHERE h.id IN (SELECT h2.id FROM Harvest h2)")
    List<Harvest> findAllWithDetails();


    @Query("SELECT h FROM Harvest h LEFT JOIN FETCH h.harvestDetails WHERE h.id = :id")
    Optional<Harvest> findByIdWithDetails(Long id);

    boolean existsBySeasonAndDate(SeasonEnum season, LocalDate date);

    @Query("SELECT SUM(h.totalQuantity) FROM Harvest h WHERE h.date BETWEEN :startDate AND :endDate")
    Double sumTotalQuantityBetweenDates(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(h) > 0 FROM Harvest h " +
            "WHERE h.season = :season " +
            "AND YEAR(h.date) = :year")
    boolean existsBySeasonAndYear(SeasonEnum season, int year);

    @Query("SELECT COUNT(h) > 0 FROM Harvest h " +
            "WHERE h.season = :season " +
            "AND YEAR(h.date) = :year " +
            "AND h.id != :harvestId")
    boolean existsBySeasonAndYearAndIdNot(
            SeasonEnum season,
            int year,
            Long harvestId
    );
}