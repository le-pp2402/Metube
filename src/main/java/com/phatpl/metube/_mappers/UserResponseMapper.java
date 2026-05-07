package com.phatpl.metube._mappers;

import com.phatpl.metube._dtos.response.UserResponse;
import com.phatpl.metube._models.User;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserResponseMapper extends BaseMapper<User, UserResponse> {
    UserResponseMapper instance = Mappers.getMapper(UserResponseMapper.class);
}
