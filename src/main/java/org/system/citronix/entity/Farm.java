package org.system.citronix.entity;

import jakarta.persistence.*;
import lombok.*;
import static org.system.citronix.constant.CitronixConstants.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "farms")
public class Farm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Double area;

    @Column(name = "creation_date", nullable = false)
    private LocalDate creationDate;

    @OneToMany(mappedBy = "farm", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Field> fields;

    public boolean canAddField(Double fieldArea) {
        double currentTotalArea = fields.stream()
                .mapToDouble(Field::getArea)
                .sum();

        return (currentTotalArea + fieldArea) < this.area;
    }

    public boolean isValidFieldArea(Double fieldArea) {
        return fieldArea <= (this.area * MAX_FIELD_PERCENTAGE);
    }
}