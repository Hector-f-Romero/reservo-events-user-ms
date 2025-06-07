package com.hector.eventuserms.events;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hector.eventuserms.events.dtos.request.CreateEventRequestDto;
import com.hector.eventuserms.events.dtos.response.CreateEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindUpcomingEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindEventsResponseDto;
import com.hector.eventuserms.events.dtos.response.FindOneEventResponseDto;
import com.hector.eventuserms.events.models.Event;
import com.hector.eventuserms.exception.AppError;
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

        System.out.println("FIND UPCOMING SIN FECHA");

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

    public List<Object> findUpcomingEventsByDate(Instant userDate) {
        return eventRepository.findUpcomingEventsByDate(userDate);
    }

    @Transactional
    public FindOneEventResponseDto findOne(UUID id) {
        // 1. Try to find and event in DB
        Event eventDB = eventRepository.findById(id)
                .orElseThrow(() -> new AppError("Event not found.", HttpStatus.NOT_FOUND));

        // return EventMapper.INSTANCE.toCreateEventResponseDto(eventDB);
        return EventMapper.INSTANCE.toFindOneEventDto(eventDB);
    }

    @Transactional
    public CreateEventResponseDto create(CreateEventRequestDto createEventDto) {

        // 1. First find the organized user by ID
        User organizer = userRepository.findById(createEventDto.organizedBy())
                .orElseThrow(() -> new AppError(
                        "User with id" + createEventDto.organizedBy() + " not found.", HttpStatus.NOT_FOUND));

        // 2. Validate that the new event is scheduled in a non-occupied slot.
        var upcomingEvents = this.findUpcomingEventsByDate(createEventDto.date());

        if (upcomingEvents.contains(createEventDto.date())) {
            throw new AppError(
                    "There is an event registered for the date " + createEventDto.date(), HttpStatus.BAD_REQUEST);
        }

        // 3. Map the dto to JPA class
        Event mappedEvent = EventMapper.INSTANCE.toEntity(createEventDto);

        // 4. Put the User object in organizedBy property.
        mappedEvent.setOrganizedBy(organizer);

        // 5. Save the event document in DB.
        Event eventDB = eventRepository.save(mappedEvent);

        // 6. Create the seats related to event.
        List<Seat> seatsList = createEventDto.seats()
                .stream()
                .map(seatTag -> Seat.builder().tag(seatTag).state(SeatState.AVAILABLE).event(eventDB).build())
                .collect(Collectors.toList());

        // 7. Save the seats data in DB.
        List<Seat> seatsDB = seatRepository.saveAll(seatsList);

        // 8. Adds seat information to the previously created event object.
        eventDB.setSeats(seatsDB);

        // 9. Convert db object into DTo
        CreateEventResponseDto responseEvent = EventMapper.INSTANCE.toCreateEventResponseDto(eventDB);

        return responseEvent;
    }
}
