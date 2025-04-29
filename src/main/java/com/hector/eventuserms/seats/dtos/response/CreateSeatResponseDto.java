package com.hector.eventuserms.seats.dtos.response;

import java.util.UUID;

import com.hector.eventuserms.seats.enums.SeatState;
import com.hector.eventuserms.seats.models.Seat;

public record CreateSeatResponseDto(
                UUID id,
                String tag,
                SeatState state,
                UUID eventId) {

        public CreateSeatResponseDto(Seat seat) {
                this(seat.getId(), seat.getTag(), seat.getState(), seat.getEvent().getId());
        }
}
