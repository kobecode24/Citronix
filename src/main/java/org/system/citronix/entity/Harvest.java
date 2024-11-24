package org.system.citronix.entity;

import jakarta.persistence.*;
import lombok.*;
import org.system.citronix.enums.SeasonEnum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "harvests")
public class Harvest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeasonEnum season;

    @Column(name = "total_quantity", nullable = false)
    private Double totalQuantity = 0.0;

    @OneToMany(mappedBy = "harvest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HarvestDetail> harvestDetails = new ArrayList<>();

    @OneToOne(mappedBy = "harvest", cascade = CascadeType.ALL)
    private Sale sales;

    public void calculateTotalQuantity() {
        this.totalQuantity = harvestDetails.stream()
                .mapToDouble(HarvestDetail::getQuantity)
                .sum();
    }

    public boolean isValidSeason(LocalDate date) {
        return season.equals(SeasonEnum.fromDate(date));
    }

    public boolean isSold() {
        return sales != null;
    }
}