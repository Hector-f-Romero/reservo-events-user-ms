package com.hector.crud.seats.dtos;

import java.util.UUID;

import com.hector.crud.seats.enums.SeatState;

public record FindSeatInListDto(
        UUID id,
        String tag,
        SeatState state) {

}
