package com.hector.eventuserms.users;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;
import com.hector.eventuserms.users.dtos.UserDto;
import com.hector.eventuserms.users.dtos.requests.CreateUserRequestDto;
import com.hector.eventuserms.users.dtos.requests.LoginUserRequestDto;
import com.hector.eventuserms.users.dtos.requests.UpdateUserNatsRequestDto;
import com.hector.eventuserms.users.dtos.requests.UpdateUserRequestDto;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Component
public class UserNatsController {

    private final UserService userService;
    private final NatsMessageProcessor natsMessageProcessor;
    private final ObjectMapper objectMapper;
    private final Connection connection;

    private final static String SUBJECT_BASE = "users.";

    private final static String SUBJECT_GET_ALL = SUBJECT_BASE + "get.all";
    private final static String SUBJECT_GET_ID = SUBJECT_BASE + "get.id";
    private final static String SUBJECT_CREATE = SUBJECT_BASE + "create";
    private final static String SUBJECT_UPDATE = SUBJECT_BASE + "update";
    private final static String SUBJECT_DELETE = SUBJECT_BASE + "delete";
    private final static String SUBJECT_LOGIN = SUBJECT_BASE + "login";

    public UserNatsController(UserService userService, NatsMessageProcessor natsMessageProcessor,
            ObjectMapper objectMapper, Connection connection) {
        this.userService = userService;
        this.natsMessageProcessor = natsMessageProcessor;
        this.objectMapper = objectMapper;
        this.connection = connection;
    }

    @PostConstruct
    public void initialize() {
        Dispatcher dispatcher = connection.createDispatcher();

        // Register all subscribes.
        dispatcher.subscribe(SUBJECT_GET_ALL, (msg) -> handleGetUsers(msg));
        dispatcher.subscribe(SUBJECT_GET_ID, (msg) -> handleGetUser(msg));
        dispatcher.subscribe(SUBJECT_CREATE, (msg) -> handleCreateUser(msg));
        dispatcher.subscribe(SUBJECT_UPDATE, (msg) -> handleUpdateUser(msg));
        dispatcher.subscribe(SUBJECT_DELETE, (msg) -> handleDeleteUser(msg));
        dispatcher.subscribe(SUBJECT_LOGIN, (msg) -> handleLoginUser(msg));
    }

    private void handleGetUsers(Message msg) {
        try {
            // 1. Use the service.
            List<UserDto> users = userService.find();

            // 2. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, users);
        } catch (Exception e) {
            natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleGetUser(Message msg) {
        try {

            // 1. Parse the data received from NATS.
            UUID id = UUID.fromString(this.natsMessageProcessor.extractDataFromJson(msg).asText());

            // 2. Use the service.
            UserDto user = userService.findOne(id);

            // 3. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, this.objectMapper.writeValueAsString(user));
        } catch (Exception e) {
            natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleCreateUser(Message msg) {
        try {

            // 1. Parse the data received from NATS.
            JsonNode payload = this.natsMessageProcessor.extractDataFromJson(msg);
            CreateUserRequestDto createUserRequestDto = this.objectMapper.treeToValue(payload,
                    CreateUserRequestDto.class);

            // 2. Use the service.
            UserDto newUser = userService.create(createUserRequestDto);

            // 3. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, this.objectMapper.writeValueAsString(newUser));
        } catch (Exception e) {
            natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleUpdateUser(Message msg) {
        try {

            // 1. Parse the data received from NATS.
            JsonNode payload = this.natsMessageProcessor.extractDataFromJson(msg);
            UpdateUserNatsRequestDto updateUserNatsRequestDto = this.objectMapper.treeToValue(payload,
                    UpdateUserNatsRequestDto.class);

            // 2. HACER MAGIA ✨
            UpdateUserRequestDto updateUserRequestDto = this.objectMapper.convertValue(updateUserNatsRequestDto,
                    UpdateUserRequestDto.class);

            // 2. Use the service.
            UserDto updatedUser = userService.update(updateUserNatsRequestDto.id(), updateUserRequestDto);

            // 3. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, this.objectMapper.writeValueAsString(updatedUser));
        } catch (Exception e) {
            natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleDeleteUser(Message msg) {
        try {

            // 1. Parse the data received from NATS.
            UUID id = UUID.fromString(this.natsMessageProcessor.extractDataFromJson(msg).asText());

            // 2. Use the service.
            userService.delete(id);

            // 3. Send the data to NATS
            this.natsMessageProcessor.sendResponse(msg, "User deleted successfully.");
        } catch (Exception e) {
            natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }

    private void handleLoginUser(Message msg) {
        try {

            // 1. Parse the data received from NATS.
            LoginUserRequestDto loginUserRequestDto = this.objectMapper
                    .treeToValue(this.natsMessageProcessor.extractDataFromJson(msg), LoginUserRequestDto.class);

            // 2. Use the service.
            UserDto loggedUser = userService.login(loginUserRequestDto);

            // 3. Send the data to NATS.
            this.natsMessageProcessor.sendResponse(msg, this.objectMapper.writeValueAsString(loggedUser));
        } catch (Exception e) {
            System.out.println("ERROR ------------------------");
            System.out.println(e);

            natsMessageProcessor.sendError(msg, e.getMessage());
        }
    }
}
