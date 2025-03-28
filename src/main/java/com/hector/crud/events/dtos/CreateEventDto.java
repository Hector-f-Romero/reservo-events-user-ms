package com.hector.crud.events.dtos;

import java.time.ZonedDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateEventDto(
        @NotBlank(message = "name cannot be empty.") @Size(min = 3, max = 80) String name,
        @NotBlank(message = "description cannot be empty.") @Size(min = 3, max = 500) String description,
        @NotNull(message = "date cannot be empty.") ZonedDateTime date,
        @NotNull(message = "capacity cannot be empty.") @Positive(message = "capacity must be a positive number") Short capacity,
        @NotNull(message = "organizedBy cannot be empty.") UUID organizedBy) {

}
