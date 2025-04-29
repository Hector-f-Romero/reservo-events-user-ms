package com.hector.eventuserms.seats.dtos;

import java.util.UUID;

import com.hector.eventuserms.seats.enums.SeatState;
import com.hector.eventuserms.seats.models.Seat;

public record SeatSummaryDto(
                UUID id,
                String tag,
                SeatState state) {

        public SeatSummaryDto(Seat seat) {
                this(seat.getId(), seat.getTag(), seat.getState());
        }
}
