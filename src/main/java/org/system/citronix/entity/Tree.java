package org.system.citronix.entity;

import jakarta.persistence.*;
import lombok.*;
import static org.system.citronix.constant.CitronixConstants.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "trees")
public class Tree {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plant_date", nullable = false)
    private LocalDate plantDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @OneToMany(mappedBy = "tree", cascade = CascadeType.ALL)
    private List<HarvestDetail> harvestDetails;

    // Business methods
    public int getAge(LocalDate referenceDate) {
        return Period.between(plantDate, referenceDate).getYears();
    }

    // Calculate productivity at a specific date
    public double getProductivity(LocalDate referenceDate) {
        int age = getAge(referenceDate);
        if (age > MAX_TREE_AGE) return 0.0;
        if (age > MATURE_TREE_AGE_LIMIT) return OLD_TREE_PRODUCTIVITY;
        if (age >= YOUNG_TREE_AGE_LIMIT) return MATURE_TREE_PRODUCTIVITY;
        return YOUNG_TREE_PRODUCTIVITY;
    }

    public boolean isValidPlantingDate(LocalDate plantDate) {
        int month = plantDate.getMonthValue();
        return month >= PLANTING_START_MONTH &&
                month <= PLANTING_END_MONTH;
    }

    public boolean isProductiveAge(LocalDate referenceDate) {
        return getAge(referenceDate) <= MAX_TREE_AGE;
    }
}