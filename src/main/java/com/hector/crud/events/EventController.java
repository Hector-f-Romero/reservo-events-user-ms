package com.hector.crud.events;

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
import org.springframework.web.bind.annotation.RestController;

import com.hector.crud.events.dtos.request.CreateEventRequestDto;
import com.hector.crud.events.dtos.response.CreateEventResponseDto;
import com.hector.crud.events.dtos.response.FindUpcomingEventResponseDto;
import com.hector.crud.events.dtos.response.FindOneEventResponseDto;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@Validated
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping()
    public ResponseEntity<?> getEvents() {
        return ResponseEntity.ok(eventService.find());
        // return ResponseEntity.ok("ok");
    }

    @GetMapping("/upcoming")
    public List<FindUpcomingEventResponseDto> getMethodName() {
        return eventService.findUpcoming();
    }

    @GetMapping("/{id}")
    public FindOneEventResponseDto getEventDto(@Valid @PathVariable UUID id) {
        return this.eventService.findOne(id);
    }

    @PostMapping()
    public ResponseEntity<CreateEventResponseDto> createEvent(
            @Valid @RequestBody() CreateEventRequestDto event) {
        return ResponseEntity.ok(eventService.create(event));
    }
}
