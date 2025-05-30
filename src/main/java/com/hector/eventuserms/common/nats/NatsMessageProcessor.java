package com.hector.eventuserms.common.nats;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        JsonNode dataNode = rootNode.get("data");

        if (dataNode == null) {
            throw new JsonParseException("Missing required 'data' field in message");
        }

        // 5. Return the "data" field.
        return dataNode;

    }

    /**
     * Converts a {@link JsonNode} into an instance of the specified type.
     * <p>
     * Any exception thrown during the deserialization is wrapped and rethrown
     * as a custom unchecked {@link AppError}, allowing it to be intercepted
     * by AOP mechanisms without requiring explicit try/catch blocks.
     *
     * @param node      the JsonNode to convert
     * @param valueType the target class type
     * @param <T>       the type of the resulting object
     * @return the deserialized object of type T
     * @throws AppError if the conversion fails
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

            // 1. Convert the objet in JsonNde to attach it inside JSON response.
            JsonNode payload = this.objectMapper.valueToTree(response);

            // 2. Manually build a JSON response to NATS.
            // We use .set() to insert a JSON Node while .put() is used to insert a string
            // in 'value' JSON field.
            ObjectNode natsResponseMessage = this.objectMapper.createObjectNode()
                    .put("status", HttpStatus.OK.name())
                    .put("code", HttpStatus.OK.value())
                    .set("message", payload);

            // 3. Serialize the response object into a JSON byte array.
            byte[] responseData = this.objectMapper.writeValueAsBytes(natsResponseMessage);

            // 4. Publish the response to the replyTo subject provided in the original
            // message.
            System.out.println(responseData);
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
            ObjectNode errorPayload = this.objectMapper.createObjectNode()
                    .put("status", error.status().name())
                    .put("message", error.message())
                    .put("code", error.status().value())
                    .put("path", error.path());

            String jsonError = this.objectMapper.writeValueAsString(errorPayload);

            // 2. Send the error as UTF-8 bytes to the replyTo subject
            natsConnection.publish(message.getReplyTo(), jsonError.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sending error response: " + jsonError);
        } catch (Exception e) {
            System.err.println("Error sending error response: " + e.getMessage());
        }
    }
}
