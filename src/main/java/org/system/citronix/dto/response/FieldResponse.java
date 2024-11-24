package org.system.citronix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FieldResponse {
    private Long id;
    private Double area;
    private Long farmId;
    private List<TreeInFieldResponse> trees;
    private int maximumTreeCapacity;
    private int availableTreeSpaces;
}