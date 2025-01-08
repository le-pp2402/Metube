package com.phatpl.metube.mappers;

import com.phatpl.metube.dtos.response.LoginResponse;
import com.phatpl.metube.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LoginResponseMapper extends BaseMapper<User, LoginResponse> {
    LoginResponseMapper instance = Mappers.getMapper(LoginResponseMapper.class);
}
