package com.hector.eventuserms.events.nats;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hector.eventuserms.common.annotations.NatsHandler;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.events.EventService;
import com.hector.eventuserms.events.dtos.response.FindOneEventResponseDto;

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
    @EventListener(condition = "#e.subject() == T(com.hector.eventuserms.events.nats.EventSubjects).GET_ID")
    public void handleGetEvent(NatsMessageEvent e) throws JsonMappingException, JsonProcessingException {
        UUID id = UUID.fromString(natsMessageProcessor.extractDataFromJson(e.msg()).asText());

        System.out.println("BUSCANDING EVENTO");
        log.info("MAMA GUEVO");
        // 2. Use the service.
        FindOneEventResponseDto event = eventService.findOne(id);

        // 3. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg(), this.natsMessageProcessor.objectToJson(event));

    }
}
