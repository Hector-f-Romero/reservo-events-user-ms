package com.hector.crud.seats;

import java.util.UUID;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hector.crud.seats.dtos.CreateSeatDto;
import com.hector.crud.seats.dtos.SeatDto;
import com.hector.crud.seats.dtos.UpdateSeatDto;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@Validated
@RequestMapping("/seats")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/{id}")
    public SeatDto getSeatById(@Valid @PathVariable UUID id) {
        return this.seatService.findOne(id);
    }

    @PostMapping()
    public SeatDto createSeat(@Valid @RequestBody() CreateSeatDto createSeatDto) {
        return this.seatService.create(createSeatDto);
    }

    @PatchMapping("/{id}")
    public SeatDto updateSeat(@Valid @PathVariable UUID id, @Valid @RequestBody UpdateSeatDto updateSeatDto) {
        return this.seatService.update(id, updateSeatDto);
    }

}
