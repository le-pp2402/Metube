package com.phatpl.metube._mappers;

import com.phatpl.metube._dtos.response.LiveSessionResponse;
import com.phatpl.metube._models.LiveSession;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LiveSessionMapper extends BaseMapper<LiveSession, LiveSessionResponse> {

    @Mapping(source = "user.username", target = "username")
    LiveSessionResponse toDTO(LiveSession session);
}
