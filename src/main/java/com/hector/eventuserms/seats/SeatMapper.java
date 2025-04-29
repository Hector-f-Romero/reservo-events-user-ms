package com.hector.eventuserms.seats;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.hector.eventuserms.seats.dtos.SeatSummaryDto;
import com.hector.eventuserms.seats.dtos.response.CreateSeatResponseDto;
import com.hector.eventuserms.seats.models.Seat;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    SeatMapper INSTANCE = Mappers.getMapper(SeatMapper.class);

    // @Mapping(source = "eventId", target = "event")
    // // @Mapping(source = "eventId", target = "event.id")
    // Seat toEntity(CreateSeatDto createSeatDto);

    @Mapping(source = "event.id", target = "eventId")
    CreateSeatResponseDto toSeatDto(Seat seat);

    @Mapping(source = "event.id", target = "eventId")
    List<CreateSeatResponseDto> toSeatDtoList(List<Seat> seats);

    List<SeatSummaryDto> toListSeatSummaryDto(List<CreateSeatResponseDto> createSeatResponseDto);
}
