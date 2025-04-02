package com.hector.crud.seats.dtos;

import java.util.UUID;

import com.hector.crud.seats.enums.SeatState;
import com.hector.crud.seats.models.Seat;

public record SeatSummaryDto(
                UUID id,
                String tag,
                SeatState state) {

        public SeatSummaryDto(Seat seat) {
                this(seat.getId(), seat.getTag(), seat.getState());
        }
}
