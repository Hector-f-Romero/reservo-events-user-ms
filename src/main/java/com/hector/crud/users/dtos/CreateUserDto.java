package com.hector.crud.users.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDto(
        @NotBlank(message = "Name cannot be empty.") String name,
        @NotBlank(message = "Username cannot be empty.") @Size(max = 15) String username,
        @NotBlank(message = "Email cannot be empty.") String email,
        @NotBlank(message = "Password cannot be empty.") String password) {

}
