package com.hector.crud.events.dtos.response;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.hector.crud.seats.dtos.SeatSummaryDto;
import com.hector.crud.users.dtos.UserDto;

public record FindOneEventResponseDto(
        UUID id,
        String name,
        String description,
        ZonedDateTime date,
        short capacity,
        UserDto organizedBy,
        List<SeatSummaryDto> seats) {

}
