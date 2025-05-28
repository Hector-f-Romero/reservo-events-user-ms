package com.hector.eventuserms.common.nats;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hector.eventuserms.exception.ApiError;
import com.hector.eventuserms.exception.AppError;

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
        JsonNode dataNode = rootNode.get("data1");

        if (dataNode == null) {
            throw new JsonParseException("Missing required 'data' field in message");
        }

        // 5. Return the "data" field.
        return dataNode;

    }

    // Este método tiene la funcionalidad de convertir una excepción checked en una
    // unchecked. Gracias a esto, estas excepciones unchecked se pueden tratar por
    // el aspecto sin declara un bloque try/catch.
    public String objectToJson(Object object) throws JsonProcessingException {
        return this.objectMapper.writeValueAsString(object);

    }

    /**
     * Convierte un JsonNode a una instancia del tipo deseado, manejando excepciones
     * como unchecked.
     */
    public <T> T fromJsonTree(JsonNode node, Class<T> valueType) {
        try {
            return this.objectMapper.treeToValue(node, valueType);
        } catch (Exception e) {
            throw new AppError("Failed to map JSON to " + valueType.getSimpleName(), HttpStatus.INTERNAL_SERVER_ERROR,
                    e);
        }
    }

    /**
     * Sends a serialized JSON response to the sender of the original message using
     * the replyTo field.
     *
     * @param msg      The original NATS message containing the replyTo property.
     * @param response The response object to serialize and send back.
     * @throws JsonProcessingException If serialization of the response fails.
     */
    public void sendResponse(Message msg, Object response) {
        try {
            // 1. Serialize the response object into a JSON byte array.
            byte[] responseData = this.objectMapper.writeValueAsBytes(response);

            // 2. Publish the response to the replyTo subject provided in the original
            // message.
            natsConnection.publish(msg.getReplyTo(), responseData);
        } catch (Exception e) {
            throw new AppError(e);
        }
    }

    /**
     * Sends an error response in JSON format to the sender of the message.
     *
     * @param message The original NATS message with the replyTo address.
     * @param error   A string message describing the error.
     */
    public void sendError(Message message, ApiError error) {
        try {

            // 1. Manually build a JSON error string.
            Map<String, Object> errorPayload = new HashMap<>();
            errorPayload.put("status", error.status().name());
            errorPayload.put("message", error.message());
            errorPayload.put("code", error.status().value());

            String error1 = this.objectMapper.writeValueAsString(errorPayload);

            // 2. Send the error as UTF-8 bytes to the replyTo subject
            natsConnection.publish(message.getReplyTo(), error1.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sending error response: " + error1);
        } catch (Exception e) {
            System.err.println("Error sending error response: " + e.getMessage());
        }
    }
}
