package com.hector.eventuserms.seats;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hector.eventuserms.events.EventRepository;
import com.hector.eventuserms.events.models.Event;
import com.hector.eventuserms.exception.AppServiceException;
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

        return SeatMapper.INSTANCE.toSeatDto(seatDB);
    }

    @Transactional
    public CreateSeatResponseDto create(CreateSeatRequestDto createSeatDto) {

        // 1. Search in DB the eventId
        Event eventDB = eventRepository.findById(createSeatDto.eventId()).orElseThrow(
                () -> new ResourceNotFoundException("Event with id " + createSeatDto.eventId() + " not found."));

        // 2. Verify that no more seats are created than the event allows.
        if (eventDB.getSeats().size() >= (int) eventDB.getCapacity()) {
            throw new AppServiceException(HttpStatus.CONFLICT,
                    "The event has reached its maximum capacity. Can't create more seats.");
        }

        // 3 Ensure not seat exists with the same tag as the one provided in the
        // request.
        boolean tagExists = eventDB.getSeats().stream().anyMatch(seat -> seat.getTag().equals(createSeatDto.tag()));

        if (tagExists) {
            throw new AppServiceException(HttpStatus.CONFLICT,
                    "A seat with the tag " + createSeatDto.tag() + " already exists.");
        }

        // 4. Save in BD.
        Seat newSeat = seatRepository.save(Seat.builder()
                .tag(createSeatDto.tag())
                .state(SeatState.AVAILABLE)
                .event(eventDB)
                .build());

        return SeatMapper.INSTANCE.toSeatDto(newSeat);
    }

    @Transactional
    public List<CreateSeatResponseDto> createMany(List<String> seats, UUID eventId) {

        // 1. Ensure that not duplicate seat tags exist in the request.
        boolean hasDuplicates = seats.size() != new HashSet<>(seats).size();

        if (hasDuplicates) {
            throw new AppServiceException(HttpStatus.BAD_REQUEST,
                    "Duplicate seat tags are not allowed. Please review your request.");
        }

        // 2. Verify that exists an event with the id sent.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event with " + eventId + " not found"));

        // 3. Verify that no more seats are created than the event allows.
        if (event.getSeats().size() >= event.getCapacity()) {
            throw new AppServiceException(HttpStatus.CONFLICT,
                    "The event has reached its maximum capacity. Can't create more seats.");
        }

        // 4. Ensure that number of seats to be inserted doesn't exceed the event's
        // capacity.
        if (event.getSeats().size() + seats.size() > event.getCapacity()) {
            throw new AppServiceException(HttpStatus.CONFLICT,
                    "The number of seats you are trying to insert  exceeds the maxium capacity of the event.");
        }

        // 5. Get the existing tags from the event inside a Set. This is helpful to
        // optimize the search.
        Set<String> existingTags = event.getSeats().stream().map(Seat::getTag).collect(Collectors.toSet());

        // 6. Ensure not seat exists with the same tag as the one provided in the
        // request.
        Optional<String> duplicateTag = seats.stream().filter(tag -> existingTags.contains(tag)).findFirst();

        // 7. If a duplicate taghas been found, throw an error.
        if (duplicateTag.isPresent()) {
            throw new AppServiceException(HttpStatus.CONFLICT,
                    "A seat with the tag " + duplicateTag.get() + " already exists.");
        }

        // 8. Create the SeatDB object with the list sent
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
