package org.system.citronix.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmRequest {
    @NotBlank(message = "Farm name is required")
    private String name;

    @NotBlank(message = "Farm location is required")
    private String location;

    @NotNull(message = "Farm area is required")
    @Positive(message = "Farm area must be positive")
    private Double area;

    @NotNull(message = "Creation date is required")
    private LocalDate creationDate;
}