package org.system.citronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.system.citronix.entity.Farm;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    @Query("SELECT f FROM Farm f WHERE f.area >= :minArea")
    List<Farm> findByAreaGreaterThanEqual(Double minArea);

    @Query("SELECT f FROM Farm f WHERE f.creationDate BETWEEN :startDate AND :endDate")
    List<Farm> findByCreationDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByName(String name);

    @Query("SELECT COUNT(f) FROM Farm f WHERE f.area > :area")
    long countFarmsLargerThan(Double area);

    @Query("SELECT f FROM Farm f LEFT JOIN FETCH f.fields WHERE f.id = :id")
    Farm findByIdWithFields(Long id);
}