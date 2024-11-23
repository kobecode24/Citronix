package org.system.citronix.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.system.citronix.enums.SeasonEnum;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HarvestRequest {
    @NotNull(message = "Harvest date is required")
    private LocalDate date;

    @NotNull(message = "Season is required")
    private SeasonEnum season;
}