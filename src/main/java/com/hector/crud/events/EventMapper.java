package com.hector.crud.events;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.hector.crud.events.dtos.request.CreateEventRequestDto;
import com.hector.crud.events.dtos.response.CreateEventResponseDto;
import com.hector.crud.events.dtos.response.FindEventsResponseDto;
import com.hector.crud.events.dtos.response.FindOneEventResponseDto;
import com.hector.crud.events.models.Event;;

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

    @Mapping(source = "event.organizedBy", target = "organizedBy")
    FindEventsResponseDto toFindEventResponseDto(Event event);
}
