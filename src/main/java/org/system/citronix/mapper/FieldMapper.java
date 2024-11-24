package org.system.citronix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.system.citronix.dto.request.FieldRequest;
import org.system.citronix.dto.response.FieldResponse;
import org.system.citronix.entity.Field;

@Mapper(componentModel = "spring", uses = {TreeMapper.class})
public interface FieldMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "trees", ignore = true)
    Field toEntity(FieldRequest request);

    @Mapping(target = "trees", ignore = true)
    void updateFieldFromRequest(FieldRequest request, @MappingTarget Field field);

    @Named("toBasicResponse")
    @Mapping(target = "farmId", source = "farm.id")
    @Mapping(target = "trees", ignore = true)
    FieldResponse toResponse(Field field);

    @Named("toDetailedResponse")
    @Mapping(target = "farmId", source = "farm.id")
    @Mapping(target = "trees", source = "trees")
    FieldResponse toResponseWithTrees(Field field);
}