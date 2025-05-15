package com.hector.eventuserms.events;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import com.hector.eventuserms.events.dtos.request.CreateEventRequestDto;
import com.hector.eventuserms.events.dtos.response.CreateEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindUpcomingEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindEventsResponseDto;
import com.hector.eventuserms.events.dtos.response.FindOneEventResponseDto;
import com.hector.eventuserms.events.models.Event;
import com.hector.eventuserms.exception.ResourceNotFoundException;
import com.hector.eventuserms.seats.SeatRepository;
import com.hector.eventuserms.seats.enums.SeatState;
import com.hector.eventuserms.seats.models.Seat;
import com.hector.eventuserms.users.UserRepository;
import com.hector.eventuserms.users.models.User;

import jakarta.transaction.Transactional;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SeatRepository seatRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
            SeatRepository seatRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public List<FindEventsResponseDto> find() {
        return eventRepository.findAll().stream()
                .map(event -> {
                    // Convert each event to a DTO, excluding seat details.
                    return EventMapper.INSTANCE.toFindEventResponseDto(event);
                }).collect(Collectors.toList());
    }

    public List<FindUpcomingEventResponseDto> findUpcoming() {
        var results = eventRepository.findUpcomingEventsWithAvailableSeats(ZonedDateTime.now());

        List<FindUpcomingEventResponseDto> upcomingEventList = results.stream()
                .map(result -> {
                    UUID id = (UUID) result[0];
                    String name = (String) result[1];
                    String description = (String) result[2];
                    ZonedDateTime date = ((Instant) result[3]).atZone(ZoneId.systemDefault());
                    short capacity = ((Number) result[4]).shortValue();
                    short occupiedSeats = ((Number) result[5]).shortValue();
                    String organizedBy = (String) result[6];

                    return new FindUpcomingEventResponseDto(id, name, description, date, capacity, occupiedSeats,
                            organizedBy);
                }).collect(Collectors.toList());

        return upcomingEventList;
    }

    public List<Object> findUpcomingEventsToday(OffsetDateTime userDate) {
        return eventRepository.findUpcomingEventsToday(userDate);
    }

    @Transactional
    public FindOneEventResponseDto findOne(UUID id) {
        // 1. Try to find and event in DB
        Event eventDB = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));

        // return EventMapper.INSTANCE.toCreateEventResponseDto(eventDB);
        return EventMapper.INSTANCE.toFindOneEventDto(eventDB);
    }

    @Transactional
    public CreateEventResponseDto create(CreateEventRequestDto createEventDto) {

        // 1. First find the organized user by ID
        User organizer = userRepository.findById(createEventDto.organizedBy())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id" + createEventDto.organizedBy() + " not found."));

        // 2. Map the dto to JPA class
        Event mappedEvent = EventMapper.INSTANCE.toEntity(createEventDto);

        // 3. Put the User object in organizedBy property.
        mappedEvent.setOrganizedBy(organizer);

        // TODO: create validation to avoid create an event in the same date.

        // 4. Save the event document in DB.
        Event eventDB = eventRepository.save(mappedEvent);

        // 5. Create the seats related to event.
        List<Seat> seatsList = createEventDto.seats()
                .stream()
                .map(seatTag -> Seat.builder().tag(seatTag).state(SeatState.AVAILABLE).event(eventDB).build())
                .collect(Collectors.toList());

        // 6. Save the seats data in DB.
        List<Seat> seatsDB = seatRepository.saveAll(seatsList);

        // 7. Adds seat information to the previously created event object.
        eventDB.setSeats(seatsDB);

        // 8. Convert db object into DTo
        CreateEventResponseDto responseEvent = EventMapper.INSTANCE.toCreateEventResponseDto(eventDB);

        return responseEvent;
    }
}
