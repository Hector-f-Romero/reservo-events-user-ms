package com.hector.eventuserms.users.nats;

import java.util.List;
import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hector.eventuserms.common.annotations.NatsHandler;
import com.hector.eventuserms.common.nats.NatsMessageProcessor;

import com.hector.eventuserms.events.nats.NatsMessageEvent;
import com.hector.eventuserms.users.UserService;
import com.hector.eventuserms.users.dtos.UserDto;
import com.hector.eventuserms.users.dtos.requests.CreateUserRequestDto;
import com.hector.eventuserms.users.dtos.requests.LoginUserRequestDto;
import com.hector.eventuserms.users.dtos.requests.UpdateUserNatsRequestDto;
import com.hector.eventuserms.users.dtos.requests.UpdateUserRequestDto;

import io.nats.client.Message;

@Component
public class UserNatsController {

    private final NatsMessageProcessor natsMessageProcessor;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserNatsController(NatsMessageProcessor natsMessageProcessor, UserService userService,
            ObjectMapper objectMapper) {
        this.natsMessageProcessor = natsMessageProcessor;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.users.nats.UserSubjects).GET_ALL")
    public void handleGetUsers(UserNatsMessage e) throws JsonMappingException, JsonProcessingException {
        // 1. Use the service.
        List<UserDto> users = userService.find();

        // 2. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, users);
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.users.nats.UserSubjects).GET_ID")
    public void handleGetUser(UserNatsMessage e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        UUID id = UUID.fromString(this.natsMessageProcessor.extractDataFromJson(e.msg).asText());

        // 2. Use the service.
        UserDto user = userService.findOne(id);

        // 3. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, user);

    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.users.nats.UserSubjects).CREATE")
    public void handleCreateUser(UserNatsMessage e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        JsonNode payload = this.natsMessageProcessor.extractDataFromJson(e.msg);
        CreateUserRequestDto createUserRequestDto = this.natsMessageProcessor.fromJsonTree(payload,
                CreateUserRequestDto.class);

        // 2. Use the service.
        UserDto newUser = userService.create(createUserRequestDto);

        // 3. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, newUser);

    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.users.nats.UserSubjects).UPDATE")
    public void handleUpdateUser(UserNatsMessage e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        JsonNode payload = this.natsMessageProcessor.extractDataFromJson(e.msg);
        UpdateUserNatsRequestDto updateUserNatsRequestDto = this.natsMessageProcessor.fromJsonTree(payload,
                UpdateUserNatsRequestDto.class);

        // 2. HACER MAGIA ✨
        // Convert the NATS object into DTO that service can use.
        UpdateUserRequestDto updateUserRequestDto = this.objectMapper.convertValue(updateUserNatsRequestDto,
                UpdateUserRequestDto.class);

        // 3. Use the service.
        UserDto updatedUser = userService.update(updateUserNatsRequestDto.id(), updateUserRequestDto);

        // 4. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, updatedUser);
    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.users.nats.UserSubjects).DELETE")
    public void handleDeleteUser(UserNatsMessage e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        UUID id = UUID.fromString(this.natsMessageProcessor.extractDataFromJson(e.msg).asText());

        // 2. Use the service.
        userService.delete(id);

        // 3. Send the data to NATS
        this.natsMessageProcessor.sendResponse(e.msg, "User deleted successfully.");

    }

    @NatsHandler
    @EventListener(condition = "#e.subject == T(com.hector.eventuserms.users.nats.UserSubjects).LOGIN")
    public void handleLoginUser(UserNatsMessage e) throws JsonMappingException, JsonProcessingException {

        // 1. Parse the data received from NATS.
        LoginUserRequestDto loginUserRequestDto = this.natsMessageProcessor
                .fromJsonTree(this.natsMessageProcessor.extractDataFromJson(e.msg), LoginUserRequestDto.class);

        // 2. Use the service.
        UserDto loggedUser = userService.login(loginUserRequestDto);

        // 3. Send the data to NATS.
        this.natsMessageProcessor.sendResponse(e.msg, this.objectMapper.writeValueAsString(loggedUser));

    }

}
