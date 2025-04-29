package com.hector.eventuserms.users;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.hector.eventuserms.users.dtos.UserDto;
import com.hector.eventuserms.users.dtos.requests.CreateUserRequestDto;
import com.hector.eventuserms.users.models.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizedEvents", ignore = true)
    User toEntity(CreateUserRequestDto createUserDto);

    UserDto toUserDto(User user);
}
