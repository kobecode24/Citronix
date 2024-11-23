package org.system.citronix.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.system.citronix.enums.SeasonEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HarvestResponse {
    private Long id;
    private LocalDate date;
    private SeasonEnum season;
    private Double totalQuantity;
    private List<HarvestDetailResponse> harvestDetails;
    private List<SaleResponse> sales;
}