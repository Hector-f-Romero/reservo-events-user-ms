package com.hector.eventuserms.events;

import java.util.Optional;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.hector.eventuserms.events.dtos.request.CreateEventRequestDto;
import com.hector.eventuserms.events.dtos.response.CreateEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindEventsResponseDto;
import com.hector.eventuserms.events.dtos.response.FindOneEventResponseDto;
import com.hector.eventuserms.events.models.Event;
import com.hector.eventuserms.users.models.User;;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizedBy", ignore = true)
    @Mapping(target = "seats", ignore = true)
    Event toEntity(CreateEventRequestDto createEventDto);

    @Mapping(source = "event.seats", target = "seats")
    @Mapping(source = "event.organizedBy", target = "organizedBy")
    CreateEventResponseDto toCreateEventResponseDto(Event event);

    @Mapping(source = "organizedBy", target = "organizedBy")
    @Mapping(source = "seats", target = "seats")
    FindOneEventResponseDto toFindOneEventDto(Event event);

    // Default method to convert a User object to Optional<UUID>. This method is
    // used in toFindOneEventDto().
    default Optional<UUID> userToOptionalUUID(User user) {
        return Optional.ofNullable(user).map(userDB -> userDB.getId());
    }

    @Mapping(source = "event.organizedBy", target = "organizedBy")
    FindEventsResponseDto toFindEventResponseDto(Event event);
}
