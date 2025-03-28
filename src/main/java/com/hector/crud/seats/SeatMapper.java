package com.hector.crud.seats;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.hector.crud.seats.dtos.CreateSeatDto;
import com.hector.crud.seats.dtos.SeatDto;
import com.hector.crud.seats.models.Seat;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    SeatMapper INSTANCE = Mappers.getMapper(SeatMapper.class);

    // @Mapping(source = "eventId", target = "event")
    // // @Mapping(source = "eventId", target = "event.id")
    // Seat toEntity(CreateSeatDto createSeatDto);

    @Mapping(source = "event.id", target = "eventId")
    SeatDto toSeatDto(Seat seat);
}
