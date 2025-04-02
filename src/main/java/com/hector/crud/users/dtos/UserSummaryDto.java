package com.hector.crud.users.dtos;

import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String name,
        String username) {

}
