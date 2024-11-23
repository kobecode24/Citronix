package org.system.citronix.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeRequest {
    @NotNull(message = "Plant date is required")
    private LocalDate plantDate;

    @NotNull(message = "Field ID is required")
    private Long fieldId;
}