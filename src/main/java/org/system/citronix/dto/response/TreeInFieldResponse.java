package org.system.citronix.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreeInFieldResponse {
    private Long id;
    private LocalDate plantDate;
    private Long fieldId;
    private int age;
    private double productivity;
}
