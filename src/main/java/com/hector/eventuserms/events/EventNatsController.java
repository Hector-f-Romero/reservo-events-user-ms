package com.hector.eventuserms.events;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.events.dtos.request.CreateEventRequestDto;
import com.hector.eventuserms.events.dtos.response.CreateEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindEventsResponseDto;
import com.hector.eventuserms.events.dtos.response.FindOneEventResponseDto;
import com.hector.eventuserms.events.dtos.response.FindUpcomingEventResponseDto;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Component
public class EventNatsController {

    private final EventService eventService;
    private final Connection natsConnection;
    private final ObjectMapper objectMapper;
    private final NatsMessageProcessor natsMessageProcessor;

    private static final String SUBJECT_BASE = "events.";

    private static final String SUBJECT_GET_ALL_EVENTS = SUBJECT_BASE + "get.all";
    private static final String SUBJECT_GET_UPCOMING_EVENTS = SUBJECT_BASE + "get.upcoming";
    private static final String SUBJECT_GET_UPCOMING_EVENTS_TODAY = SUBJECT_BASE + "get.upcoming.today";
    private static final String SUBJECT_GET_ID = SUBJECT_BASE + "get.id";
    private static final String SUBJECT_CREATE = SUBJECT_BASE + "create";

    private static final Logger log = LoggerFactory.getLogger(EventNatsController.class);

    public EventNatsController(EventService eventService, Connection natsConnection, ObjectMapper objectMapper,
            NatsMessageProcessor natsMessageProcessor) {
        this.eventService = eventService;
        this.natsConnection = natsConnection;
        this.objectMapper = objectMapper;
        this.natsMessageProcessor = natsMessageProcessor;
    }

    @PostConstruct
    public void initialize() {
        Dispatcher dispatcher = natsConnection.createDispatcher();

        // Register all subscribes.
        dispatcher.subscribe(SUBJECT_GET_ALL_EVENTS, (msg) -> handleGetEvents(msg));
        dispatcher.subscribe(SUBJECT_GET_UPCOMING_EVENTS, (msg) -> handleGetUpcomingEvents(msg));
        dispatcher.subscribe(SUBJECT_GET_UPCOMING_EVENTS_TODAY, (msg) -> handleGetUpcomingEventsToday(msg));
        dispatcher.subscribe(SUBJECT_GET_ID, (msg) -> handleGetEvent(msg));
        dispatcher.subscribe(SUBJECT_CREATE, (msg) -> handleCreateEvent(msg));
    }

    private void handleGetEvents(Message msg) {
        try {
            // 1. Use the service.
            List<FindEventsResponseDto> events = eventService.find();

            // 2. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, events);
        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleGetUpcomingEvents(Message msg) {
        try {
            // 1. Use the service.
            List<FindUpcomingEventResponseDto> events = eventService.findUpcoming();

            // 2. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, events);
        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleGetUpcomingEventsToday(Message msg) {
        try {

            // 1. Parse the data received from NATS.
            OffsetDateTime date = OffsetDateTime.parse(this.natsMessageProcessor.extractDataFromJson(msg).asText());

            // 2. Use the service to return upcoming events today.
            var result = eventService.findUpcomingEventsToday(date);

            // 3. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, result);

        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleGetEvent(Message msg) {
        try {

            UUID id = UUID.fromString(natsMessageProcessor.extractDataFromJson(msg).asText());

            // 2. Use the service.
            FindOneEventResponseDto event = eventService.findOne(id);

            // 3. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleCreateEvent(Message msg) {
        try {
            // 1. Parse the data received from NATS.
            JsonNode payload = natsMessageProcessor.extractDataFromJson(msg);
            CreateEventRequestDto createEventRequestDto = this.objectMapper.treeToValue(payload,
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
            this.natsMessageProcessor.sendResponse(msg, objectMapper.writeValueAsString(newEvent));
        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

}
