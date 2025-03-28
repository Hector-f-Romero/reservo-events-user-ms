package com.hector.crud.events;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.hector.crud.events.dtos.CreateEventDto;
import com.hector.crud.events.dtos.EventDto;
import com.hector.crud.events.dtos.FindOneEventDto;
import com.hector.crud.events.models.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventMapper INSTANCE = Mappers.getMapper(EventMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizedBy", ignore = true)
    Event toEntity(CreateEventDto createEventDto);

    EventDto ToEventDto(Event event);

    @Mapping(source = "organizedBy", target = "organizedBy")
    @Mapping(source = "seats", target = "seats")
    FindOneEventDto toFindOneEventDto(Event event);
}
