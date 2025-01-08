package com.phatpl.metube.mappers;

import com.phatpl.metube.dtos.response.UserResponse;
import com.phatpl.metube.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserResponseMapper extends BaseMapper<User, UserResponse> {
    UserResponseMapper instance = Mappers.getMapper(UserResponseMapper.class);
}
