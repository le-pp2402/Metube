package com.phatpl.metube.mappers;

import com.phatpl.metube.dtos.request.identity.RegisterRequest;
import com.phatpl.metube.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RegisterRequestMapper extends BaseMapper<User, RegisterRequest> {
}
