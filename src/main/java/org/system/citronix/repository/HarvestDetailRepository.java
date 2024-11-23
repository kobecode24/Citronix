package org.system.citronix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.system.citronix.entity.HarvestDetail;
import org.system.citronix.enums.SeasonEnum;

import java.util.List;

@Repository
public interface HarvestDetailRepository extends JpaRepository<HarvestDetail, Long> {
    List<HarvestDetail> findByHarvestId(Long harvestId);

    List<HarvestDetail> findByTreeId(Long treeId);

    @Query("SELECT SUM(hd.quantity) FROM HarvestDetail hd WHERE hd.harvest.id = :harvestId")
    Double sumQuantityByHarvestId(Long harvestId);

    @Query("SELECT COUNT(hd) > 0 FROM HarvestDetail hd " +
            "WHERE hd.tree.id = :treeId " +
            "AND hd.harvest.season = :season " +
            "AND YEAR(hd.harvest.date) = :year")
    boolean existsByTreeIdAndHarvestSeasonAndYear(Long treeId, SeasonEnum season, int year);
}