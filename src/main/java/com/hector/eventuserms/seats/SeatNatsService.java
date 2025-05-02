package com.hector.eventuserms.seats;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hector.eventuserms.seats.dtos.SeatSummaryDto;

import io.nats.client.Connection;

@Service
public class SeatNatsService {

    private final Connection connection;
    private final ObjectMapper objectMapper;

    public SeatNatsService(Connection connection, ObjectMapper objectMapper) {
        this.connection = connection;
        this.objectMapper = objectMapper;
    }

    public void sendUpdateSeatStatusEvent(UUID eventId, SeatSummaryDto updatedSeatDto)
            throws JsonProcessingException {

        // Create the data structure to send the event.
        Map<String, Object> updatedSeatInfo = new HashMap<>();
        updatedSeatInfo.put("eventId", eventId);
        updatedSeatInfo.put("seatId", updatedSeatDto.id());
        updatedSeatInfo.put("seatState", updatedSeatDto.state());

        String jsonPayload = this.objectMapper.writeValueAsString(updatedSeatInfo);

        this.connection.publish("update.seat.state", jsonPayload.getBytes(StandardCharsets.UTF_8));

    }
}
