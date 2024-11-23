package org.system.citronix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.system.citronix.dto.request.HarvestDetailRequest;
import org.system.citronix.dto.response.HarvestDetailResponse;
import org.system.citronix.entity.HarvestDetail;

@Mapper(componentModel = "spring")
public interface HarvestDetailMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "harvest", ignore = true)
    @Mapping(target = "tree", ignore = true)
    HarvestDetail toEntity(HarvestDetailRequest request);

    void updateHarvestDetailFromRequest(HarvestDetailRequest request, @MappingTarget HarvestDetail harvestDetail);

    @Mapping(target = "harvestId", source = "harvest.id")
    @Mapping(target = "treeId", source = "tree.id")
    HarvestDetailResponse toResponse(HarvestDetail harvestDetail);
}