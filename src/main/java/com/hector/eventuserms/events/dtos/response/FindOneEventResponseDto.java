package com.hector.eventuserms.events.dtos.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.hector.eventuserms.seats.dtos.ReservedSeatDto;
import com.hector.eventuserms.users.dtos.UserDto;

public record FindOneEventResponseDto(
                UUID id,
                String name,
                String description,
                Instant date,
                short capacity,
                UserDto organizedBy,
                List<ReservedSeatDto> seats) {

}
