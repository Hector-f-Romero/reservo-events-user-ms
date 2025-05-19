package com.hector.eventuserms.seats;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.seats.dtos.request.CreateManySeatsNatsDto;
import com.hector.eventuserms.seats.dtos.request.CreateSeatRequestDto;
import com.hector.eventuserms.seats.dtos.request.UpdateSeatNatsRequestDto;
import com.hector.eventuserms.seats.dtos.response.CreateSeatResponseDto;
import com.hector.eventuserms.seats.dtos.response.UpdateSeatResponseDto;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Component
public class SeatNatsController {

    private final NatsMessageProcessor natsMessageProcessor;
    private final SeatService seatService;
    private final Connection connection;
    private final ObjectMapper objectMapper;

    private static final String SUBJECT_BASE = "seats.";

    private static final String SUBJECT_GET_ID = SUBJECT_BASE + "get.id";
    private static final String SUBJECT_CREATE = SUBJECT_BASE + "create";
    private static final String SUBJECT_CREATE_ALL = SUBJECT_BASE + "create.all";
    private static final String SUBJECT_UPDATE = SUBJECT_BASE + "update";

    public SeatNatsController(NatsMessageProcessor natsMessageProcessor, SeatService seatService, Connection connection,
            ObjectMapper objectMapper) {
        this.natsMessageProcessor = natsMessageProcessor;
        this.seatService = seatService;
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initialize() {
        Dispatcher dispatcher = this.connection.createDispatcher();

        // Register all subscribes.
        dispatcher.subscribe(SUBJECT_GET_ID, (msg) -> handleGetSeat(msg));
        dispatcher.subscribe(SUBJECT_CREATE, (msg) -> handleCreateSeat(msg));
        dispatcher.subscribe(SUBJECT_CREATE_ALL, (msg) -> handleCreateAllSeats(msg));
        dispatcher.subscribe(SUBJECT_UPDATE, (msg) -> handleUpdateSeat(msg));
    }

    public void handleGetSeat(Message msg) {
        try {
            // 1. Parse the data received from NATS.
            UUID id = UUID.fromString(this.natsMessageProcessor.extractDataFromJson(msg).asText());

            // 2. Use the service to return a seat by ID.
            CreateSeatResponseDto seat = this.seatService.findOne(id);

            // 3. Send the data to NATS.
            this.natsMessageProcessor.sendResponse(msg, this.objectMapper.writeValueAsString(seat));

        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    public void handleCreateSeat(Message msg) {
        try {

            // 1. Parse the data received from NATS.
            JsonNode payload = this.natsMessageProcessor.extractDataFromJson(msg);
            CreateSeatRequestDto createSeatRequestDto = this.objectMapper.treeToValue(payload,
                    CreateSeatRequestDto.class);

            // 2. Use the service to create a seat.
            CreateSeatResponseDto newSeat = this.seatService.create(createSeatRequestDto);

            // 3. Send the data to NATS.
            this.natsMessageProcessor.sendResponse(msg, this.objectMapper.writeValueAsString(newSeat));
        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }

    }

    public void handleCreateAllSeats(Message msg) {
        try {

            // 1. Parse the data received from NATS.
            JsonNode payload = this.natsMessageProcessor.extractDataFromJson(msg);
            CreateManySeatsNatsDto createSeatRequestDto = this.objectMapper.treeToValue(payload,
                    CreateManySeatsNatsDto.class);

            // 2. Use the service.
            List<CreateSeatResponseDto> newSeats = this.seatService.createMany(createSeatRequestDto.seats(),
                    createSeatRequestDto.eventId());

            // 3. Send the data to NATS.
            this.natsMessageProcessor.sendResponse(msg, this.objectMapper.writeValueAsString(newSeats));

        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    public void handleUpdateSeat(Message msg) {
        try {
            // 1. Parse the data received from NATS.
            JsonNode payload = this.natsMessageProcessor.extractDataFromJson(msg);
            UpdateSeatNatsRequestDto updateSeatNatsRequestDto = this.objectMapper.treeToValue(payload,
                    UpdateSeatNatsRequestDto.class);

            // 2. Use the service.
            UpdateSeatResponseDto newSeat = this.seatService.update(updateSeatNatsRequestDto.id(),
                    updateSeatNatsRequestDto.updateSeatRequestDto());

            // 3. Send the data to NATS.
            this.natsMessageProcessor.sendResponse(msg, this.objectMapper.writeValueAsString(newSeat));
        } catch (Exception e) {
            this.natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

}
