package com.hector.eventuserms.seats.dtos.request;

import java.util.List;
import java.util.UUID;

public record CreateManySeatsNatsDto(

        List<String> seats,
        UUID eventId) {

}
