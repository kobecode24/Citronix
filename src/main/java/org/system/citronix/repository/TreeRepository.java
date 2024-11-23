package org.system.citronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.system.citronix.entity.Tree;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TreeRepository extends JpaRepository<Tree, Long> {
    List<Tree> findByFieldId(Long fieldId);

    @Query("SELECT t FROM Tree t WHERE t.plantDate BETWEEN :startDate AND :endDate")
    List<Tree> findByPlantDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Tree t WHERE t.field.id = :fieldId")
    long countTreesByFieldId(Long fieldId);

    @Query(value = "SELECT * FROM trees WHERE EXTRACT(YEAR FROM AGE(CURRENT_DATE, plant_date)) > :age",
            nativeQuery = true)
    List<Tree> findTreesOlderThan(@Param("age") int age);

    @Query("SELECT t FROM Tree t LEFT JOIN FETCH t.harvestDetails WHERE t.id = :id")
    Tree findByIdWithHarvestDetails(Long id);

    @Query("SELECT COUNT(t) FROM Tree t WHERE t.field.id = :fieldId AND " +
            "t.plantDate BETWEEN :startDate AND :endDate")
    long countTreesPlantedInPeriod(Long fieldId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT t FROM Tree t WHERE t.field.farm.id = :farmId")
    List<Tree> findAllTreesByFarmId(@Param("farmId") Long farmId);
}