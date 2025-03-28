package com.hector.crud.seats;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hector.crud.events.EventRepository;
import com.hector.crud.events.models.Event;
import com.hector.crud.exception.ResourceNotFoundException;
import com.hector.crud.seats.dtos.CreateSeatDto;
import com.hector.crud.seats.dtos.SeatDto;
import com.hector.crud.seats.dtos.UpdateSeatDto;
import com.hector.crud.seats.enums.SeatState;
import com.hector.crud.seats.models.Seat;
import com.hector.crud.users.models.User;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    public SeatService(SeatRepository seatRepository, EventRepository eventRepository) {
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
    }

    public SeatDto findOne(UUID id) {

        // 1. Try to find the user in DB.
        Seat seatDB = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id " + id + " not found."));

        return SeatMapper.INSTANCE.toSeatDto(seatDB);
    }

    public SeatDto create(CreateSeatDto createSeatDto) {

        // 1. Search in DB the eventId
        Event eventDB = eventRepository.findById(createSeatDto.eventId()).orElseThrow(
                () -> new ResourceNotFoundException("Event with id " + createSeatDto.eventId() + " not found."));

        // 2. Save in BD.
        Seat newSeat = seatRepository.save(Seat.builder()
                .tag(createSeatDto.tag())
                .state(SeatState.AVAILABLE)
                .event(eventDB)
                .build());

        return SeatMapper.INSTANCE.toSeatDto(newSeat);
    }

    public SeatDto update(UUID id, UpdateSeatDto updateSeatDto) {

        Seat seatDB = this.seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id " + id + " not found"));

        // 2. Update only non-null fields.
        if (updateSeatDto.tag() != null) {
            seatDB.setTag(updateSeatDto.tag());
        }

        if (updateSeatDto.state() != null) {
            seatDB.setState(updateSeatDto.state());
        }

        // 3. Save the changes in DB.
        Seat updatedUser = seatRepository.save(seatDB);

        return SeatMapper.INSTANCE.toSeatDto(updatedUser);
    }
}
