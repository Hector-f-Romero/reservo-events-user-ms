package com.hector.eventuserms.seed.nats;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hector.eventuserms.common.annotations.NatsHandler;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.seed.SeedService;

@Component
public class SeedNatsController {

    private final NatsMessageProcessor natsMessageProcessor;
    private final SeedService seedService;

    public SeedNatsController(NatsMessageProcessor natsMessageProcessor, SeedService seedService) {
        this.natsMessageProcessor = natsMessageProcessor;
        this.seedService = seedService;
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.seed.nats.SeedSubjects).EXECUTE_SEED")
    public void handleGetSeat(NatsMessageSeed e) throws JsonMappingException, JsonProcessingException {

        // 1. Execute the seed
        this.seedService.executeSeed();

        this.natsMessageProcessor.sendResponse(e.msg, "Seed executed.");
    }

}
