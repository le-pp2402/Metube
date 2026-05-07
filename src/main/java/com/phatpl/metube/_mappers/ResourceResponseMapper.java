package com.phatpl.metube._mappers;

import com.phatpl.metube._dtos.response.ResourceResponse;
import com.phatpl.metube._models.Resource;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResourceResponseMapper extends BaseMapper<Resource, ResourceResponse> {
    ResourceResponseMapper instance = Mappers.getMapper(ResourceResponseMapper.class);
}
