package com.hector.crud.seats.dtos.response;

import java.util.UUID;

import com.hector.crud.seats.enums.SeatState;
import com.hector.crud.seats.models.Seat;

public record CreateSeatResponseDto(
                UUID id,
                String tag,
                SeatState state,
                UUID eventId) {

        public CreateSeatResponseDto(Seat seat) {
                this(seat.getId(), seat.getTag(), seat.getState(), seat.getEvent().getId());
        }
}
