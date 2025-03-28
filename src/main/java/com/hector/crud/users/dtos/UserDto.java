package com.hector.crud.users.dtos;

import java.util.UUID;

import com.hector.crud.users.models.User;

public record UserDto(
        UUID id,
        String name,
        String username,
        String email) {

    public UserDto(User user) {
        this(user.getId(), user.getName(), user.getUsername(), user.getEmail());
    }
}
