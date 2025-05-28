// package com.hector.eventuserms.events;

// import java.time.Instant;
// import java.util.List;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.ApplicationContext;
// import org.springframework.stereotype.Component;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.hector.eventuserms.common.annotations.NatsHandler;
// import com.hector.eventuserms.common.nats.NatsMessageProcessor;
// import com.hector.eventuserms.events.dtos.request.CreateEventRequestDto;
// import com.hector.eventuserms.events.dtos.response.CreateEventResponseDto;
// import com.hector.eventuserms.events.dtos.response.FindEventsResponseDto;
// import com.hector.eventuserms.events.dtos.response.FindOneEventResponseDto;
// import
// com.hector.eventuserms.events.dtos.response.FindUpcomingEventResponseDto;

// import io.nats.client.Connection;
// import io.nats.client.Dispatcher;
// import io.nats.client.Message;
// import jakarta.annotation.PostConstruct;
// import lombok.extern.slf4j.Slf4j;

// @Component
// @Slf4j
// public class EventNatsController {

// private final EventService eventService;
// private final Connection natsConnection;
// private final NatsMessageProcessor natsMessageProcessor;

// private static final String SUBJECT_BASE = "events.";

// private static final String SUBJECT_GET_ALL_EVENTS = SUBJECT_BASE +
// "get.all";
// private static final String SUBJECT_GET_UPCOMING_EVENTS = SUBJECT_BASE +
// "get.upcoming";
// private static final String SUBJECT_GET_UPCOMING_EVENTS_BY_DATE =
// SUBJECT_BASE + "get.upcoming.date";
// private static final String SUBJECT_GET_ID = SUBJECT_BASE + "get.id";
// private static final String SUBJECT_CREATE = SUBJECT_BASE + "create";

// public EventNatsController(EventService eventService, Connection
// natsConnection,
// NatsMessageProcessor natsMessageProcessor) {
// this.eventService = eventService;
// this.natsConnection = natsConnection;
// this.natsMessageProcessor = natsMessageProcessor;
// }

// @PostConstruct
// public void initialize() {
// Dispatcher dispatcher = natsConnection.createDispatcher();

// // Register all subscribes.
// dispatcher.subscribe(SUBJECT_GET_ALL_EVENTS, (msg) -> handleGetEvents(msg));
// dispatcher.subscribe(SUBJECT_GET_UPCOMING_EVENTS, (msg) ->
// handleGetUpcomingEvents(msg));
// dispatcher.subscribe(SUBJECT_GET_UPCOMING_EVENTS_BY_DATE, (msg) ->
// handleGetUpcomingEventsByDate(msg));
// dispatcher.subscribe(SUBJECT_GET_ID, this::handleGetEvent);
// dispatcher.subscribe(SUBJECT_CREATE, (msg) -> handleCreateEvent(msg));
// }

// @NatsHandler(subject = SUBJECT_GET_ALL_EVENTS)
// private void handleGetEvents(Message msg) {

// // 1. Use the service.
// List<FindEventsResponseDto> events = eventService.find();

// // 2. Send the data to NATS
// this.natsMessageProcessor.sendResponse(msg, events);

// }

// @NatsHandler(subject = SUBJECT_GET_UPCOMING_EVENTS)
// private void handleGetUpcomingEvents(Message msg) {

// // 1. Use the service.
// List<FindUpcomingEventResponseDto> events = eventService.findUpcoming();

// // 2. Send the data to NATS
// this.natsMessageProcessor.sendResponse(msg, events);

// }

// @NatsHandler(subject = SUBJECT_GET_UPCOMING_EVENTS_BY_DATE)
// private void handleGetUpcomingEventsByDate(Message msg) {

// // 1. Parse the data received from NATS.
// Instant date =
// Instant.parse(this.natsMessageProcessor.extractDataFromJson(msg).asText());

// // 2. Use the service to return upcoming events today.
// var result = eventService.findUpcomingEventsByDate(date);

// // 3. Send the data to NATS
// this.natsMessageProcessor.sendResponse(msg, result);

// }

// @NatsHandler(subject = SUBJECT_CREATE)
// private void handleCreateEvent(Message msg) {

// // 1. Parse the data received from NATS.
// JsonNode payload = natsMessageProcessor.extractDataFromJson(msg);
// CreateEventRequestDto createEventRequestDto =
// this.natsMessageProcessor.fromJsonTree(payload,
// CreateEventRequestDto.class);

// // 2. Use the service.
// CreateEventResponseDto newEvent = eventService.create(createEventRequestDto);

// // * Note If we use `this.natsMessageProcessor.sendResponse(msg, newEvent);`
// /*
// * NATS will publish the data, but the NestJS client won't be able to receive
// * it, due to the
// * payload is not a valid "string" or "Uint8Arrays".
// *
// * See:
// https://nats-io.github.io/nats.js/core/index.html#publish-and-subscribe
// *
// * For that reason, we need to convert the class object into a String or send
// it
// * inside a List, like:
// * `this.natsMessageProcessor.sendResponse(msg, Arrays.asList(newEvent))` ->
// * This
// * works
// *
// *
// * Honestly... I’m impressed this is necessary. LMAO
// */
// // 3. Send the data to NATS
// this.natsMessageProcessor.sendResponse(msg,
// this.natsMessageProcessor.objectToJson(newEvent));

// }

// }
