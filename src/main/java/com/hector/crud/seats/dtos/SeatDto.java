package com.hector.crud.seats.dtos;

import java.util.UUID;

import com.hector.crud.seats.enums.SeatState;
import com.hector.crud.seats.models.Seat;

public record SeatDto(
                UUID id,
                String tag,
                SeatState state,
                UUID eventId) {

        public SeatDto(Seat seat) {
                this(seat.getId(), seat.getTag(), seat.getState(), seat.getEvent().getId());
        }
}
