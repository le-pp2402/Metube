package com.phatpl.metube.mappers;

import com.phatpl.metube.models.Resource;
import com.phatpl.metube.services.ResourceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResourceResponseMapper extends BaseMapper<Resource, ResourceResponse> {
    ResourceResponseMapper instance = Mappers.getMapper(ResourceResponseMapper.class);
}
