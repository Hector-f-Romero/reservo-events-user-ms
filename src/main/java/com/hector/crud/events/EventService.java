package com.hector.crud.events;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import com.hector.crud.CrudApplication;

import com.hector.crud.events.dtos.CreateEventDto;
import com.hector.crud.events.dtos.EventDto;
import com.hector.crud.events.dtos.FindOneEventDto;
import com.hector.crud.events.models.Event;
import com.hector.crud.exception.ResourceNotFoundException;
import com.hector.crud.users.UserRepository;
import com.hector.crud.users.dtos.UserDto;
import com.hector.crud.users.models.User;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
            CrudApplication crudApplication) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public List<EventDto> find() {
        return eventRepository.findAll()
                .stream()
                .map(EventDto::new)
                .collect(Collectors.toList());
    }

    public FindOneEventDto findOne(UUID id) {
        // 1. Try to find and event in DB
        Event eventDB = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));

        // return EventMapper.INSTANCE.ToEventDto(eventDB);
        return EventMapper.INSTANCE.toFindOneEventDto(eventDB);

        // return new EventDto(id, eventDB.getName(), eventDB.getDescription(),
        // eventDB.getDate(), eventDB.getCapacity(),
        // new UserDto(eventDB.getOrganizedBy().getId(),
        // eventDB.getOrganizedBy().getName(),
        // eventDB.getOrganizedBy().getUsername(),
        // eventDB.getOrganizedBy().getEmail()));
    }

    public EventDto create(CreateEventDto createEventDto) {

        // 1. First find the organized user by ID
        User organizer = userRepository.findById(createEventDto.organizedBy())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id" + createEventDto.organizedBy() + " not found."));

        // 2. Map the dto to JPA class
        Event mappedEvent = EventMapper.INSTANCE.toEntity(createEventDto);

        mappedEvent.setOrganizedBy(organizer);
        // 3. Save the document in DB.
        Event eventDB = eventRepository.save(mappedEvent);

        // 4. Convert db object into DTo
        EventDto responseEvent = EventMapper.INSTANCE.ToEventDto(eventDB);

        return responseEvent;
    }
}
