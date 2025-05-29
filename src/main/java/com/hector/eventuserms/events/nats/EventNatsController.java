package com.hector.eventuserms.events.nats;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.hector.eventuserms.common.annotations.NatsHandler;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.events.EventService;
import com.hector.eventuserms.events.dtos.request.CreateEventRequestDto;
import com.hector.eventuserms.events.dtos.response.CreateEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindEventsResponseDto;
import com.hector.eventuserms.events.dtos.response.FindOneEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindUpcomingEventResponseDto;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventNatsController {

    private final NatsMessageProcessor natsMessageProcessor;
    private final EventService eventService;

    public EventNatsController(NatsMessageProcessor natsMessageProcessor, EventService eventService) {
        this.natsMessageProcessor = natsMessageProcessor;
        this.eventService = eventService;
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.events.nats.EventSubjects).GET_ALL_EVENTS")
    public void handleGetEvents(NatsMessageEvent e) throws JsonMappingException, JsonProcessingException {
        // 1. Use the service.
        List<FindEventsResponseDto> events = eventService.find();

        // 2. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, events);
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.events.nats.EventSubjects).GET_UPCOMING_EVENTS")
    public void handleGetUpcomingEvents(NatsMessageEvent e) throws JsonMappingException, JsonProcessingException {
        // 1. Use the service.
        List<FindUpcomingEventResponseDto> events = eventService.findUpcoming();

        // 2. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, events);
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.events.nats.EventSubjects).GET_UPCOMING_EVENTS_BY_DATE")
    public void handleGetUpcomingEventsByDate(NatsMessageEvent e) throws JsonMappingException, JsonProcessingException {
        // 1. Parse the data received from NATS.
        Instant date = Instant.parse(this.natsMessageProcessor.extractDataFromJson(e.msg).asText());

        log.info("ANTES DE SERVICIO");
        // 2. Use the service to return upcoming events today.
        var result = eventService.findUpcomingEventsByDate(date);

        log.info("Salí del servicio");

        // 3. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, result);
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.events.nats.EventSubjects).GET_ID")
    public void handleGetEvent(NatsMessageEvent e) throws JsonMappingException, JsonProcessingException {
        UUID id = UUID.fromString(natsMessageProcessor.extractDataFromJson(e.msg).asText());
        // 2. Use the service.
        FindOneEventResponseDto event = eventService.findOne(id);

        // 3. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, event);
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.events.nats.EventSubjects).CREATE")
    public void handleCreateEvent(NatsMessageEvent e) throws JsonMappingException, JsonProcessingException {
        // 1. Parse the data received from NATS.
        JsonNode payload = natsMessageProcessor.extractDataFromJson(e.msg);
        CreateEventRequestDto createEventRequestDto = this.natsMessageProcessor.fromJsonTree(payload,
                CreateEventRequestDto.class);

        // 2. Use the service.
        CreateEventResponseDto newEvent = eventService.create(createEventRequestDto);

        // * Note If we use `this.natsMessageProcessor.sendResponse(msg, newEvent);`
        /*
         * NATS will publish the data, but the NestJS client won't be able to receive
         * it, due to the
         * payload is not a valid "string" or "Uint8Arrays".
         * 
         * See: https://nats-io.github.io/nats.js/core/index.html#publish-and-subscribe
         * 
         * For that reason, we need to convert the class object into a String or send it
         * inside a List, like:
         * `this.natsMessageProcessor.sendResponse(msg, Arrays.asList(newEvent))` ->
         * This
         * works
         * 
         * 
         * Honestly... I’m impressed this is necessary. LMAO
         */
        // 3. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, newEvent);
    }
}
