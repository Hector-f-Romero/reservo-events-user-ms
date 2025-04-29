package com.hector.eventuserms.users;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hector.eventuserms.users.dtos.UserDto;
import com.hector.eventuserms.users.dtos.requests.CreateUserRequestDto;
import com.hector.eventuserms.users.dtos.requests.LoginUserRequestDto;
import com.hector.eventuserms.users.dtos.requests.UpdateUserRequestDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Validated
@Tag(name = "Users", description = "Operations related to user management (registration, login, update, and deletion).")
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    // private static final Logger logger =
    // LoggerFactory.getLogger(UserService.class);

    UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get active users", description = "Returns a list of all active registered users in the system.")
    @GetMapping()
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(userService.find());
    }

    @Operation(summary = "Get user by ID", description = "Returns information for a specific active user, identified by their UUID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@Valid @PathVariable UUID id) {
        return ResponseEntity.ok(userService.findOne(id));
    }

    @Operation(summary = "Create a user", description = "Receives user data and registers the user in the system.")
    @PostMapping()
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody() CreateUserRequestDto user) {
        return ResponseEntity.ok(userService.create(user));
    }

    @Operation(summary = "Login user", description = "Verifies user credentials and returns user information if correct.")
    @PostMapping("/login")
    public ResponseEntity<UserDto> loginUser(@RequestBody LoginUserRequestDto loginUserRequestDto) {
        return ResponseEntity.ok(userService.login(loginUserRequestDto));
    }

    @Operation(summary = "Update user information", description = "Allows partial modification of user data using their UUID.")
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserRequestDto user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    @Operation(summary = "Delete a user", description = "Deletes a user from the system using their UUID. This operation apply a soft delete in database, so its attribute 'isActive' will change to 'false'.")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable @Valid UUID id) {
        userService.delete(id);
        return ResponseEntity.ok("Ok");
    }
}
