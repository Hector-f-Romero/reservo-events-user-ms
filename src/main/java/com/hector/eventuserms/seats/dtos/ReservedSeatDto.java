package com.hector.eventuserms.seats.dtos;

import java.util.Optional;
import java.util.UUID;

import com.hector.eventuserms.seats.enums.SeatState;
import com.hector.eventuserms.seats.models.Seat;

public record ReservedSeatDto(
        UUID id,
        String tag,
        SeatState state,
        Optional<UUID> userId) {

    public ReservedSeatDto(Seat seat) {
        this(seat.getId(), seat.getTag(), seat.getState(),
                Optional.ofNullable(seat.getUserId()).map(user -> user.getId()));
    }
}
