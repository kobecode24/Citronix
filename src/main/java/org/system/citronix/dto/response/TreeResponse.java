package org.system.citronix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeResponse {
    private Long id;
    private LocalDate plantDate;
    private Long fieldId;
    private int age;
    private double productivity;
}