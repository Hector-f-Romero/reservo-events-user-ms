package com.hector.eventuserms.events.dtos.response;

import java.time.Instant;
import java.util.UUID;

import com.hector.eventuserms.users.dtos.UserSummaryDto;

public record FindEventsResponseDto(
        UUID id,
        String name,
        String description,
        Instant date,
        short capacity,
        UserSummaryDto organizedBy) {

}
