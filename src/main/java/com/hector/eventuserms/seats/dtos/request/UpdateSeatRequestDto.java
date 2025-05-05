package com.hector.eventuserms.seats.dtos.request;

import java.util.Optional;
import java.util.UUID;

import com.hector.eventuserms.seats.enums.SeatState;

public record UpdateSeatRequestDto(
        String tag,
        SeatState state,
        Optional<UUID> userId) {

}
