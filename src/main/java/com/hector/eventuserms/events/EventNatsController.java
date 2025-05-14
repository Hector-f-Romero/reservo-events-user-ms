package com.hector.eventuserms.events;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;

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

        dispatcher.subscribe("events.get.upcoming", (msg) -> handleGetUpcomingEventsToday(msg));
    }

    private void handleGetUpcomingEventsToday(Message msg) {
        try {
            System.out.println("RECIBIENDO POR NATS DEL EVENTO EVENTS.GETS.UPCOMING");
            // 1. Convert the data in bytes to string/JSON.
            // String requestJSON = new String(msg.getData(), StandardCharsets.UTF_8);
            // JsonNode rootNode = objectMapper.readTree(requestJSON);
            // OffsetDateTime data = OffsetDateTime.parse(rootNode.get("data").asText());

            OffsetDateTime date = OffsetDateTime.parse(this.natsMessageProcessor.extractDataFromJson(msg).asText());

            var result = eventService.findUpcomingEventsToday(date);

            // byte[] responseData = objectMapper.writeValueAsBytes(result);
            // natsConnection.publish(msg.getReplyTo(), responseData);
            this.natsMessageProcessor.sendResponse(msg, result);

        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());

            // e.printStackTrace();
            // if (msg.getReplyTo() != null) {
            // String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
            // natsConnection.publish(msg.getReplyTo(),
            // errorResponse.getBytes(StandardCharsets.UTF_8));
            // }
            // throw new Error(e.getMessage());
        }
    }

}
