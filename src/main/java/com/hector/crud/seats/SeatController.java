package com.hector.crud.seats;

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

import com.hector.crud.seats.dtos.request.CreateManySeatsRequestDto;
import com.hector.crud.seats.dtos.request.CreateSeatRequestDto;
import com.hector.crud.seats.dtos.request.UpdateSeatRequestDto;
import com.hector.crud.seats.dtos.response.CreateSeatResponseDto;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/{id}")
    public CreateSeatResponseDto getSeatById(@Valid @PathVariable UUID id) {
        return this.seatService.findOne(id);
    }

    @PostMapping()
    public CreateSeatResponseDto createSeat(@Valid @RequestBody() CreateSeatRequestDto createSeatDto) {
        return this.seatService.create(createSeatDto);
    }

    @PostMapping("/all")
    public List<CreateSeatResponseDto> creatManySeats(@RequestBody CreateManySeatsRequestDto createManySeatsDto) {
        return this.seatService.createMany(createManySeatsDto.seats(), createManySeatsDto.eventId());
    }

    @PatchMapping("/{id}")
    public CreateSeatResponseDto updateSeat(@Valid @PathVariable UUID id,
            @Valid @RequestBody UpdateSeatRequestDto updateSeatDto) {
        return this.seatService.update(id, updateSeatDto);
    }

}
