package org.system.citronix.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldRequest {
    @NotNull(message = "Field area is required")
    @Positive(message = "Field area must be positive")
    private Double area;

    @NotNull(message = "Farm ID is required")
    private Long farmId;
}