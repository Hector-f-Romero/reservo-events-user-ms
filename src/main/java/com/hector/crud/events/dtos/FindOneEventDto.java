package com.hector.crud.events.dtos;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.hector.crud.seats.dtos.FindSeatInListDto;
import com.hector.crud.users.dtos.UserDto;

public record FindOneEventDto(
                UUID id,
                String name,
                String description,
                ZonedDateTime date,
                short capacity,
                UserDto organizedBy,
                List<FindSeatInListDto> seats) {

}
