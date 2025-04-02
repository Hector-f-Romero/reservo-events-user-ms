package com.hector.crud.events.dtos.response;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.hector.crud.events.models.Event;
import com.hector.crud.seats.dtos.SeatSummaryDto;
import com.hector.crud.users.dtos.UserDto;

public record CreateEventResponseDto(
                UUID id,
                String name,
                String description,
                ZonedDateTime date,
                short capacity,
                UserDto organizedBy,
                List<SeatSummaryDto> seats) {

        public CreateEventResponseDto(Event event) {
                this(
                                event.getId(),
                                event.getName(),
                                event.getDescription(),
                                event.getDate(),
                                event.getCapacity(),
                                event.getOrganizedBy() != null
                                                ? new UserDto(event.getOrganizedBy())
                                                : null,
                                event.getSeats().stream().map(SeatSummaryDto::new).toList());
        }
}
