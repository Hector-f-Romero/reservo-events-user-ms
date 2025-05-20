package com.hector.eventuserms.events;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hector.eventuserms.events.dtos.request.CreateEventRequestDto;
import com.hector.eventuserms.events.dtos.response.CreateEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindUpcomingEventResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.hector.eventuserms.events.dtos.response.FindOneEventResponseDto;

import jakarta.validation.Valid;

@RestController
@Validated
@Tag(name = "Events", description = "Operations for managing events: querying, creation, and listing upcoming events.")
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Operation(summary = "Get all events", description = "Returns a list of all registered events in the system, excluding seat details.")
    @GetMapping()
    public ResponseEntity<?> getEvents() {
        return ResponseEntity.ok(eventService.find());
    }

    @Operation(summary = "Get upcoming event list", description = "Returns a list of upcoming events, showing only those with available seats and a future date.")
    @GetMapping("/upcoming-detailed")
    public List<FindUpcomingEventResponseDto> getUpcomingEvents() {
        return eventService.findUpcoming();
    }

    @Operation(summary = "Get upcoming events for a specific day", description = "Returns a list of events scheduled on the provided date (UTC offset must be included).")
    @GetMapping("/upcoming")
    public List<Object> getUpcomingEventsToday(
            @Valid @RequestParam("date") @Parameter(description = "Date in ISO 8601 format with time zone offset (e.g., 2025-05-13T00:00:00Z)", required = true) OffsetDateTime date) {
        return eventService.findUpcomingEventsToday(date);
    }

    @Operation(summary = "Get an event by ID", description = "Returns complete information for a specific event using its UUID.")
    @GetMapping("/{id}")
    public FindOneEventResponseDto getEventDto(@Valid @PathVariable UUID id) {
        return this.eventService.findOne(id);
    }

    @Operation(summary = "Create an event", description = "Allows registering a new event, associating it with an existing organizer, and automatically generating the related seats.")
    @PostMapping()
    public ResponseEntity<CreateEventResponseDto> createEvent(
            @Valid @RequestBody() CreateEventRequestDto event) {
        return ResponseEntity.ok(eventService.create(event));
    }
}
