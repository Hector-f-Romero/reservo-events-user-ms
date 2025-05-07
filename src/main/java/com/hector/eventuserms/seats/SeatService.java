package com.hector.eventuserms.seats;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hector.eventuserms.events.EventRepository;
import com.hector.eventuserms.events.models.Event;
import com.hector.eventuserms.exception.ResourceNotFoundException;
import com.hector.eventuserms.seats.dtos.SeatSummaryDto;
import com.hector.eventuserms.seats.dtos.request.CreateSeatRequestDto;
import com.hector.eventuserms.seats.dtos.request.UpdateSeatRequestDto;
import com.hector.eventuserms.seats.dtos.response.CreateSeatResponseDto;
import com.hector.eventuserms.seats.dtos.response.UpdateSeatResponseDto;
import com.hector.eventuserms.seats.enums.SeatState;
import com.hector.eventuserms.seats.models.Seat;
import com.hector.eventuserms.users.UserRepository;
import com.hector.eventuserms.users.models.User;

import jakarta.transaction.Transactional;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SeatNatsService seatNatsService;

    public SeatService(SeatRepository seatRepository, EventRepository eventRepository,
            SeatNatsService seatNatsService, UserRepository userRepository) {
        this.seatRepository = seatRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.seatNatsService = seatNatsService;
    }

    public CreateSeatResponseDto findOne(UUID id) {

        // 1. Try to find the user in DB.
        Seat seatDB = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id " + id + " not found."));

        System.out.println("----------------------------------");
        System.out.println(seatDB.toString());
        // System.out.println(seatDB.getUserId());

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

    @Transactional
    public UpdateSeatResponseDto update(UUID id, UpdateSeatRequestDto updateSeatDto) {

        // 1. Get the DB data.
        Seat seatDB = this.seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat with id " + id + " not found"));

        // 2. Update only non-null fields.
        if (updateSeatDto.tag() != null) {
            seatDB.setTag(updateSeatDto.tag());
        }

        if (updateSeatDto.state() != null) {
            seatDB.setState(updateSeatDto.state());
        }

        // 2.1 If a user ID is provided when updating a seat,
        // it means the user wants to reserve it.
        if (updateSeatDto.userId().isPresent()) {

            // 2.1.1 First, verify in DB if the seat is available.
            boolean isSeatAvailable = seatRepository.isAvailableToReserve(seatDB.getId());

            // 2.1.2 If the seat already has assigned a userId, return an error for client.
            if (!isSeatAvailable) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The seat " + seatDB.getTag() + " already has reserved.");
            }

            // 2.1.3 Extract the user ID from the request DTO.
            UUID userId = updateSeatDto.userId().get();

            // 2.1.4 Check if the user exists in the database.
            User userDB = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found."));

            // 2.1.5 Verify that the user has not already reserved a seat for this event
            boolean existsReserveSeat = seatRepository.existsByEventIdAndUserId(seatDB.getEvent().getId(), userId);

            // 2.1.6 If the user has already reserved a seat, throw a conflict error.
            if (existsReserveSeat) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "The user " + userId + " already has reserved a seat for this event.");
            }

            // 2.1.7 Associate the user with the seat in the db.
            seatDB.setUserId(userDB);
        }

        // 3. Save the changes in DB.
        Seat updatedSeat = seatRepository.save(seatDB);

        // 4. If the state is changed, send a notification through NATS to websocket
        // microservice.
        if (updateSeatDto.state() != null) {
            try {
                // 4.1 Use a service class specialized in NATS communication.
                this.seatNatsService.sendUpdateSeatStatusEvent(updatedSeat.getEvent().getId(),
                        new SeatSummaryDto(updatedSeat));
            } catch (Exception e) {
                // TODO: handle exception
                throw new InternalError(e.getMessage());
            }
        }
        return SeatMapper.INSTANCE.toUpdateSeatResponseDto(updatedSeat);
    }
}
