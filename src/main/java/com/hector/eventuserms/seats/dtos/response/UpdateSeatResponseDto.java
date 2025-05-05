package com.hector.eventuserms.seats.dtos.response;

import java.util.UUID;

import com.hector.eventuserms.seats.enums.SeatState;
import com.hector.eventuserms.seats.models.Seat;

public record UpdateSeatResponseDto(
        UUID id,
        String tag,
        SeatState state,
        UUID eventId,
        UUID userId) {

    public UpdateSeatResponseDto(Seat seat) {
        this(seat.getId(), seat.getTag(), seat.getState(), seat.getEvent().getId(), seat.getUserId().getId());
    }
}
