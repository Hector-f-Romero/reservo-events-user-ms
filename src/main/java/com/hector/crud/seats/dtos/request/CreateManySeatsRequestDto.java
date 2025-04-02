package com.hector.crud.seats.dtos.request;

import java.util.List;
import java.util.UUID;

public record CreateManySeatsRequestDto(
        List<String> seats,
        UUID eventId) {

}
