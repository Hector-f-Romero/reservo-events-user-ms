package com.hector.crud.seats;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hector.crud.events.EventRepository;
import com.hector.crud.events.models.Event;
import com.hector.crud.exception.ResourceNotFoundException;
import com.hector.crud.seats.dtos.request.CreateSeatRequestDto;
import com.hector.crud.seats.dtos.request.UpdateSeatRequestDto;
import com.hector.crud.seats.dtos.response.CreateSeatResponseDto;
import com.hector.crud.seats.enums.SeatState;
import com.hector.crud.seats.models.Seat;

import jakarta.transaction.Transactional;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;

    public SeatService(SeatRepository seatRepository, EventRepository eventRepository) {
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
    }

    public CreateSeatResponseDto findOne(UUID id) {

        // 1. Try to find the user in DB.
        Seat seatDB = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id " + id + " not found."));

        return SeatMapper.INSTANCE.toSeatDto(seatDB);
    }

    public CreateSeatResponseDto create(CreateSeatRequestDto createSeatDto) {

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

    @Transactional
    public List<CreateSeatResponseDto> createMany(List<String> seats, UUID eventId) {

        // 1. Verify that exists an event with the id sent.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with " + eventId + " not found"));

        // 2. Create the SeatDB object with the list sent
        List<Seat> seatsList = seats.stream()
                .map(tag -> Seat.builder()
                        .tag(tag).state(SeatState.AVAILABLE)
                        .event(event)
                        .build())
                .collect(Collectors.toList());

        seatRepository.saveAll(seatsList);

        return SeatMapper.INSTANCE.toSeatDtoList(seatsList);
    }

    public CreateSeatResponseDto update(UUID id, UpdateSeatRequestDto updateSeatDto) {

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
