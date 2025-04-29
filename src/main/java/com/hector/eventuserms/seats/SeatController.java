package com.hector.eventuserms.seats;

import java.util.List;
import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hector.eventuserms.seats.dtos.request.CreateManySeatsRequestDto;
import com.hector.eventuserms.seats.dtos.request.CreateSeatRequestDto;
import com.hector.eventuserms.seats.dtos.request.UpdateSeatRequestDto;
import com.hector.eventuserms.seats.dtos.response.CreateSeatResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@Tag(name = "Seats", description = "Operations for managing seats assigned to events.")
@RequestMapping("/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @Operation(summary = "Get seat by ID", description = "Returns detailed information for a specific seat using its UUID.")
    @GetMapping("/{id}")
    public CreateSeatResponseDto getSeatById(@Valid @PathVariable UUID id) {
        return this.seatService.findOne(id);
    }

    @Operation(summary = "Create a seat", description = "Registers a new seat in the system for a specific event.")
    @PostMapping()
    public CreateSeatResponseDto createSeat(@Valid @RequestBody() CreateSeatRequestDto createSeatDto) {
        return this.seatService.create(createSeatDto);
    }

    @Operation(summary = "Create all seats for an event", description = "Allows registering multiple seats associated with an event in a single operation.")
    @PostMapping("/all")
    public List<CreateSeatResponseDto> creatManySeats(@RequestBody CreateManySeatsRequestDto createManySeatsDto) {
        return this.seatService.createMany(createManySeatsDto.seats(), createManySeatsDto.eventId());
    }

    @Operation(summary = "Update seat information", description = "Modifies the data of an existing seat using its UUID.")
    @PatchMapping("/{id}")
    public CreateSeatResponseDto updateSeat(@Valid @PathVariable UUID id,
            @Valid @RequestBody UpdateSeatRequestDto updateSeatDto) {
        return this.seatService.update(id, updateSeatDto);
    }

}
