package org.system.citronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.system.citronix.entity.Sale;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByHarvestId(Long harvestId);

    @Query("SELECT s FROM Sale s WHERE s.date BETWEEN :startDate AND :endDate")
    List<Sale> findByDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT s FROM Sale s WHERE s.customer = :customer")
    List<Sale> findByCustomer(String customer);

    @Query("SELECT SUM(s.unitPrice * s.harvest.totalQuantity) FROM Sale s " +
            "WHERE s.date BETWEEN :startDate AND :endDate")
    Double calculateTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate);

    @Query("SELECT AVG(s.unitPrice) FROM Sale s WHERE s.harvest.season = :season")
    Double calculateAverageUnitPriceBySeason(org.system.citronix.enums.SeasonEnum season);
}