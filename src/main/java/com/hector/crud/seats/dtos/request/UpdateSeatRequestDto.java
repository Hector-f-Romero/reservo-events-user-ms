package com.hector.crud.seats.dtos.request;

import com.hector.crud.seats.enums.SeatState;

public record UpdateSeatRequestDto(
        String tag,
        SeatState state) {

}
