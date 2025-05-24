package com.phatpl.metube.mappers;

import com.phatpl.metube.dtos.response.LiveSessionResponse;
import com.phatpl.metube.models.LiveSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LiveSessionMapper extends BaseMapper<LiveSession, LiveSessionResponse> {

    @Mapping(source = "user.username", target = "username")
    LiveSessionResponse toDTO(LiveSession session);
}
