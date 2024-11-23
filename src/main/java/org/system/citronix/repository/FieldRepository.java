package org.system.citronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.system.citronix.entity.Field;

import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    List<Field> findByFarmId(Long farmId);

    @Query("SELECT f FROM Field f WHERE f.area <= :maxArea")
    List<Field> findByAreaLessThanEqual(Double maxArea);

    @Query("SELECT COUNT(f) FROM Field f WHERE f.farm.id = :farmId")
    long countFieldsByFarmId(Long farmId);

    @Query("SELECT f FROM Field f LEFT JOIN FETCH f.trees WHERE f.id = :id")
    Field findByIdWithTrees(Long id);

    @Query("SELECT SUM(f.area) FROM Field f WHERE f.farm.id = :farmId")
    Double sumAreaByFarmId(Long farmId);
}