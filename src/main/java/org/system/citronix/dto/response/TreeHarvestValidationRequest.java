package org.system.citronix.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.system.citronix.enums.SeasonEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreeHarvestValidationRequest {
    @NotNull(message = "Tree ID is required")
    private Long treeId;

    @NotNull(message = "Season is required")
    private SeasonEnum season;

    @NotNull(message = "Year is required")
    private Integer year;
}