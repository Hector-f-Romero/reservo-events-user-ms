package com.hector.eventuserms.users.dtos.requests;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserNatsRequestDto(
        UUID id,
        String name,
        @Size(max = 15) String username,
        @Email String email,
        String password

) {

}
