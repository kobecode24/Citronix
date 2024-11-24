package org.system.citronix.entity;

import jakarta.persistence.*;
import lombok.*;

import static org.system.citronix.constant.CitronixConstants.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "fields")
public class Field {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double area;  // in hectares

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Builder.Default
    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tree> trees = new ArrayList<>();

    public int getMaximumTreeCapacity() {
        return (int) (this.area * MAX_TREES_PER_HECTARE);
    }

    public int getAvailableTreeSpaces() {
        return getMaximumTreeCapacity() - trees.size();
    }
}