package com.hector.crud.seats.dtos;

import java.util.UUID;

public record CreateSeatDto(
                String tag,
                UUID eventId) {

}
