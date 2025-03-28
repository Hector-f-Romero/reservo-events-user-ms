package com.hector.crud.seats.dtos;

import com.hector.crud.seats.enums.SeatState;

public record UpdateSeatDto(
        String tag,
        SeatState state) {

}
