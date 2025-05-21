package com.hector.eventuserms.users.dtos.requests;

import java.util.Optional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDto(
        String name,
        @Size(max = 15) String username,
        @Email String email,
        String password,
        Optional<Boolean> isActive) {

}
