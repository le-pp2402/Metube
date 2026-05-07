package com.phatpl.metube._mappers;

import com.phatpl.metube._dtos.request.identity.RegisterRequest;
import com.phatpl.metube._models.User;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RegisterRequestMapper extends BaseMapper<User, RegisterRequest> {
     RegisterRequestMapper instance = Mappers.getMapper(RegisterRequestMapper.class);
}
