package com.hector.eventuserms.events.dtos.response;

import java.time.ZonedDateTime;
import java.util.UUID;

public record FindUpcomingEventResponseDto(
                UUID id,
                String name,
                String description,
                ZonedDateTime date,
                short capacity,
                short occupiedSeats,
                String organizedBy) {
}
