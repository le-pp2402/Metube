package com.phatpl.metube.mappers;

import com.phatpl.metube.dtos.response.ResourceResponse;
import com.phatpl.metube.models.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.phatpl.metube.utils.DatetimeSolver;
import java.sql.Timestamp;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ResourceResponseMapper extends BaseMapper<Resource, ResourceResponse> {
    ResourceResponseMapper instance = Mappers.getMapper(ResourceResponseMapper.class);

    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "createdAt", target = "dateTime", qualifiedByName = "timestampToString")
    @Mapping(source = "viewCount", target = "viewCount")
    @Mapping(source = "likeCount", target = "likeCount")
    ResourceResponse toDTO(Resource resource);

    @Named("timestampToString")
    default String timestampToString(Timestamp createdAt) {
        if (createdAt == null) return null;
        return DatetimeSolver.findTimeAgo(new java.util.Date(createdAt.getTime()), new java.util.Date());
    }
}
