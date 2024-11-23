package org.system.citronix.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.system.citronix.dto.request.SaleRequest;
import org.system.citronix.dto.response.SaleResponse;
import org.system.citronix.entity.Sale;

@Mapper(componentModel = "spring")
public interface SaleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "harvest", ignore = true)
    Sale toEntity(SaleRequest request);

    void updateSaleFromRequest(SaleRequest request, @MappingTarget Sale sale);

    @Mapping(target = "harvestId", source = "harvest.id")
    @Mapping(target = "revenue", expression = "java(calculateRevenue(sale))")
    SaleResponse toResponse(Sale sale);

    default Double calculateRevenue(Sale sale) {
        return sale != null && sale.getHarvest() != null ?
                sale.getUnitPrice() * sale.getHarvest().getTotalQuantity() :
                null;
    }
}