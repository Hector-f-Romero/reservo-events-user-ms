package com.hector.eventuserms.seats.dtos.request;

import java.util.UUID;

public record UpdateSeatNatsRequestDto(
        UUID id,
        UpdateSeatRequestDto updateSeatRequestDto) {

}
