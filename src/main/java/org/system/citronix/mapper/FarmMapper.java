package org.system.citronix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.system.citronix.dto.request.FarmRequest;
import org.system.citronix.dto.response.FarmResponse;
import org.system.citronix.entity.Farm;

@Mapper(componentModel = "spring", uses = {FieldMapper.class})
public interface FarmMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fields", ignore = true)
    Farm toEntity(FarmRequest request);

    @Mapping(target = "fields", ignore = true)
    void updateFarmFromRequest(FarmRequest request, @MappingTarget Farm farm);

    @Mapping(target = "fields", ignore = true)
    FarmResponse toResponse(Farm farm);

    @Mapping(target = "fields", source = "fields", qualifiedByName = "toBasicResponse")
    FarmResponse toResponseWithFields(Farm farm);
}