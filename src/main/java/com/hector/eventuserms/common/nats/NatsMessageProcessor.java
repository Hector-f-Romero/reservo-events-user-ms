package com.hector.eventuserms.common.nats;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.nats.client.Connection;
import io.nats.client.Message;

@Component
public class NatsMessageProcessor {

    private final Connection natsConnection;
    private final ObjectMapper objectMapper;

    public NatsMessageProcessor(Connection natsConnection, ObjectMapper objectMapper) {
        this.natsConnection = natsConnection;
        this.objectMapper = objectMapper;

    }

    /**
     * Extracts the value of the "data" field from a JSON-based NATS message
     * payload.
     *
     * @param msg The incoming NATS message.
     * @return A JsonNode representing the content of the "data" field.
     * @throws JsonMappingException    If the message cannot be mapped to a JSON
     *                                 structure.
     * @throws JsonProcessingException If JSON parsing fails.
     */
    public JsonNode extractDataFromJson(Message msg) throws JsonMappingException, JsonProcessingException {

        // 1. Validate that args are not malformed.
        if (msg == null || msg.getData() == null) {
            throw new IllegalArgumentException("Message or message data cannot be null or empty.");
        }

        // 2. Convert raw byte data to UTF-8 encoded string.
        String requestJSON = new String(msg.getData(), StandardCharsets.UTF_8);

        if (requestJSON.isEmpty()) {
            throw new IllegalArgumentException("Empty message payload");
        }

        // 3. Parse the string into a JsonNode tree.
        JsonNode rootNode = this.objectMapper.readTree(requestJSON);

        // 4. Get the "data" field from the root node.
        JsonNode dataNode = rootNode.get("data");

        if (dataNode == null) {
            throw new JsonParseException("Missing required 'data' field in message");
        }

        // 5. Return the "data" field.
        return dataNode;
    }

    /**
     * Sends a serialized JSON response to the sender of the original message using
     * the replyTo field.
     *
     * @param msg      The original NATS message containing the replyTo property.
     * @param response The response object to serialize and send back.
     * @throws JsonProcessingException If serialization of the response fails.
     */
    public void sendResponse(Message msg, Object response) throws JsonProcessingException {

        // 1. Serialize the response object into a JSON byte array.
        byte[] responseData = this.objectMapper.writeValueAsBytes(response);

        // 2. Publish the response to the replyTo subject provided in the original
        // message.
        natsConnection.publish(msg.getReplyTo(), responseData);
    }

    /**
     * Sends an error response in JSON format to the sender of the message.
     *
     * @param message The original NATS message with the replyTo address.
     * @param error   A string message describing the error.
     */
    public void sendError(Message message, String error) {
        try {

            // 1. Manually build a JSON error string.
            String errorResponse = "{\"error\": \"" + error + "\"}";

            // 2. Send the error as UTF-8 bytes to the replyTo subject
            natsConnection.publish(message.getReplyTo(), errorResponse.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            System.err.println("Error sending error response: " + e.getMessage());
        }
    }
}
