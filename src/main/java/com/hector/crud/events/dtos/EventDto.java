package com.hector.crud.events.dtos;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.hector.crud.events.models.Event;
import com.hector.crud.users.dtos.UserDto;

public record EventDto(
                UUID id,
                String name,
                String description,
                ZonedDateTime date,
                short capacity,
                UserDto organizedBy) {

        public EventDto(Event event) {
                this(
                                event.getId(),
                                event.getName(),
                                event.getDescription(),
                                event.getDate(),
                                event.getCapacity(),
                                event.getOrganizedBy() != null
                                                ? new UserDto(event.getOrganizedBy())
                                                : null);
        }
}
