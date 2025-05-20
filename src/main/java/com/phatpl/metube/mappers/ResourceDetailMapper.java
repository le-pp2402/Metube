package com.phatpl.metube.mappers;


import com.phatpl.metube.dtos.response.ResourceDetailDTO;
import com.phatpl.metube.models.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResourceDetailMapper extends BaseMapper<Resource, ResourceDetailDTO> {
    @Mapping(source = "viewCount", target = "viewCount")
    @Mapping(source = "likeCount", target = "likeCount")
    ResourceDetailDTO toDTO(Resource resource);
}
