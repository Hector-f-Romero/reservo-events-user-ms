package com.hector.crud.users;

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

import com.hector.crud.users.dtos.UserDto;
import com.hector.crud.users.dtos.requests.CreateUserRequestDto;
import com.hector.crud.users.dtos.requests.LoginUserRequestDto;
import com.hector.crud.users.dtos.requests.UpdateUserRequestDto;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@Validated
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    // private static final Logger logger =
    // LoggerFactory.getLogger(UserService.class);

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<List<UserDto>> getUsers() {
        return ResponseEntity.ok(userService.find());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@Valid @PathVariable UUID id) {
        return ResponseEntity.ok(userService.findOne(id));
    }

    @PostMapping()
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody() CreateUserRequestDto user) {
        return ResponseEntity.ok(userService.create(user));
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> loginUser(@RequestBody LoginUserRequestDto loginUserRequestDto) {
        return ResponseEntity.ok(userService.login(loginUserRequestDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserRequestDto user) {
        return ResponseEntity.ok(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable @Valid UUID id) {
        userService.delete(id);
        return ResponseEntity.ok("Ok");
    }
}
