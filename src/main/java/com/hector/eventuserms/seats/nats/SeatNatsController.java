package com.hector.eventuserms.seats.nats;

import java.util.List;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.hector.eventuserms.common.annotations.NatsHandler;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.seats.SeatService;
import com.hector.eventuserms.seats.dtos.request.CreateManySeatsNatsDto;
import com.hector.eventuserms.seats.dtos.request.CreateSeatRequestDto;
import com.hector.eventuserms.seats.dtos.request.UpdateSeatNatsRequestDto;
import com.hector.eventuserms.seats.dtos.response.CreateSeatResponseDto;
import com.hector.eventuserms.seats.dtos.response.UpdateSeatResponseDto;

@Component
public class SeatNatsController {

    private final NatsMessageProcessor natsMessageProcessor;
    private final SeatService seatService;

    public SeatNatsController(NatsMessageProcessor natsMessageProcessor, SeatService seatService) {
        this.natsMessageProcessor = natsMessageProcessor;
        this.seatService = seatService;
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.seats.nats.SeatSubjects).GET_ID")
    public void handleGetSeat(NatsMessageSeat e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        UUID id = UUID.fromString(this.natsMessageProcessor.extractDataFromJson(e.msg).asText());

        // 2. Use the service to return a seat by ID.
        CreateSeatResponseDto seat = this.seatService.findOne(id);

        // 3. Send the data to NATS.
        this.natsMessageProcessor.sendResponse(e.msg, seat);
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.seats.nats.SeatSubjects).CREATE")
    public void handleCreateSeat(NatsMessageSeat e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        JsonNode payload = this.natsMessageProcessor.extractDataFromJson(e.msg);
        CreateSeatRequestDto createSeatRequestDto = this.natsMessageProcessor.fromJsonTree(payload,
                CreateSeatRequestDto.class);

        // 2. Use the service to create a seat.
        CreateSeatResponseDto newSeat = this.seatService.create(createSeatRequestDto);

        // 3. Send the data to NATS.
        this.natsMessageProcessor.sendResponse(e.msg, newSeat);

    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.seats.nats.SeatSubjects).CREATE_ALL")
    public void handleCreateAllSeats(NatsMessageSeat e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        JsonNode payload = this.natsMessageProcessor.extractDataFromJson(e.msg);
        CreateManySeatsNatsDto createSeatRequestDto = this.natsMessageProcessor.fromJsonTree(payload,
                CreateManySeatsNatsDto.class);

        // 2. Use the service.
        List<CreateSeatResponseDto> newSeats = this.seatService.createMany(createSeatRequestDto.seats(),
                createSeatRequestDto.eventId());

        // 3. Send the data to NATS.
        this.natsMessageProcessor.sendResponse(e.msg, newSeats);
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.seats.nats.SeatSubjects).UPDATE")
    public void handleUpdateSeat(NatsMessageSeat e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        JsonNode payload = this.natsMessageProcessor.extractDataFromJson(e.msg);
        UpdateSeatNatsRequestDto updateSeatNatsRequestDto = this.natsMessageProcessor.fromJsonTree(payload,
                UpdateSeatNatsRequestDto.class);

        // 2. Use the service.
        UpdateSeatResponseDto updatedSeat = this.seatService.update(updateSeatNatsRequestDto.id(),
                updateSeatNatsRequestDto.updateSeatRequestDto());

        // 3. Send the data to NATS.
        this.natsMessageProcessor.sendResponse(e.msg, updatedSeat);
    }

}
