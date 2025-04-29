package com.hector.eventuserms.seats.dtos.request;

import com.hector.eventuserms.seats.enums.SeatState;

public record UpdateSeatRequestDto(
                String tag,
                SeatState state) {

}
