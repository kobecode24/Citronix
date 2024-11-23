package org.system.citronix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.system.citronix.dto.request.HarvestRequest;
import org.system.citronix.dto.response.HarvestResponse;
import org.system.citronix.entity.Harvest;

@Mapper(componentModel = "spring", uses = {HarvestDetailMapper.class, SaleMapper.class})
public interface HarvestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "harvestDetails", ignore = true)
    @Mapping(target = "sales", ignore = true)
    @Mapping(target = "totalQuantity", constant = "0.0")
    Harvest toEntity(HarvestRequest request);

    void updateHarvestFromRequest(HarvestRequest request, @MappingTarget Harvest harvest);

    @Mapping(target = "harvestDetails", ignore = true)
    @Mapping(target = "sales", ignore = true)
    HarvestResponse toResponse(Harvest harvest);

    @Mapping(target = "harvestDetails", source = "harvestDetails")
    @Mapping(target = "sales", source = "sales")
    HarvestResponse toResponseWithDetails(Harvest harvest);
}