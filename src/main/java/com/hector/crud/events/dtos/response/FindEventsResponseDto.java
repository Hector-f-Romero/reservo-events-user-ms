package com.hector.crud.events.dtos.response;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.hector.crud.users.dtos.UserSummaryDto;

public record FindEventsResponseDto(
        UUID id,
        String name,
        String description,
        ZonedDateTime date,
        short capacity,
        UserSummaryDto organizedBy) {

}
