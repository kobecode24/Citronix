package org.system.citronix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.system.citronix.dto.request.TreeRequest;
import org.system.citronix.dto.response.TreeResponse;
import org.system.citronix.entity.Tree;

import java.time.LocalDate;


@Mapper(componentModel = "spring")
public interface TreeMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "field", ignore = true)
    @Mapping(target = "harvestDetails", ignore = true)
    Tree toEntity(TreeRequest request);

    @Mapping(target = "harvestDetails", ignore = true)
    void updateTreeFromRequest(TreeRequest request, @MappingTarget Tree tree);

    @Mapping(target = "fieldId", source = "tree.field.id")
    @Mapping(target = "age", expression = "java(tree.getAge(dateProvider))")
    @Mapping(target = "productivity", expression = "java(tree.getProductivity(dateProvider))")
    TreeResponse toResponse(Tree tree, LocalDate dateProvider);
}