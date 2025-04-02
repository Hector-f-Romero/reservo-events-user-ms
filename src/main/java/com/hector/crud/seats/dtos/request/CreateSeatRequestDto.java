package com.hector.crud.seats.dtos.request;

import java.util.UUID;

public record CreateSeatRequestDto(
                String tag,
                UUID eventId) {

}
