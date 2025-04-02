package com.hector.crud.users.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDto(
        String name,
        @Size(max = 15) String username,
        @Email String email,
        String password) {

}
