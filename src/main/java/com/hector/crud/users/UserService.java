package com.hector.crud.users;

import java.util.List;
import java.util.UUID;

import org.hibernate.Session;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Service;

import com.hector.crud.exception.ResourceNotFoundException;
import com.hector.crud.users.dtos.UserDto;
import com.hector.crud.users.dtos.requests.CreateUserRequestDto;
import com.hector.crud.users.dtos.requests.LoginUserRequestDto;
import com.hector.crud.users.dtos.requests.UpdateUserRequestDto;
import com.hector.crud.users.models.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private final EntityManager entityManager;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    // TODO: avoid return documents with isActive = false
    public List<UserDto> find() {

        // 1. Activate the filter to avoid return data with isActive property in false
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("activeFilter").setParameter("isActive", true);

        return userRepository.findAll().stream()
                .map(UserDto::new)
                .toList();
    }

    // TODO: avoid return documents with isActive = false
    public UserDto findOne(UUID id) {

        // 1. Activate the filter to avoid return data with isActive property in false
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("activeFilter").setParameter("isActive", true);

        // 2. Try to find the user in DB.
        User userDB = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));

        return new UserDto(id, userDB.getName(), userDB.getUsername(), userDB.getEmail());
    }

    public UserDto create(CreateUserRequestDto createUserDto) {

        // 1. Hash the password
        String hashedPassword = passwordEncoder.encode(createUserDto.password());

        // 2. Save the user in DB.
        User userDB = userRepository.save(
                User.builder()
                        .name(createUserDto.name())
                        .username(createUserDto.username())
                        .email(createUserDto.email())
                        .password(hashedPassword).build());

        // 3. Return the mapped data.
        return UserMapper.INSTANCE.toUserDto(userDB);
        // return new UserDto(userDB.getId(), userDB.getName(), userDB.getUsername(),
        // userDB.getEmail());
    }

    @Transactional
    public UserDto update(UUID id, UpdateUserRequestDto updateUserDto) {

        // 1. Verify if exists user by ID.
        User existUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));

        // 2. Update only non-null fields.
        if (updateUserDto.name() != null) {
            existUser.setName(updateUserDto.name());
        }

        if (updateUserDto.username() != null) {
            existUser.setUsername(updateUserDto.username());
        }

        if (updateUserDto.email() != null) {
            existUser.setEmail(updateUserDto.email());
        }

        if (updateUserDto.password() != null) {
            String hashedPassword = passwordEncoder.encode(updateUserDto.password());

            existUser.setPassword(hashedPassword);
        }

        // 3. Save the changes in DB.
        User updatedUser = userRepository.save(existUser);

        // 4. Return updated document.
        return new UserDto(updatedUser.getId(), updatedUser.getName(), updatedUser.getUsername(),
                updatedUser.getEmail());
    }

    public void delete(UUID id) {

        // 1. Verify if exists user by ID.
        User existUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found."));

        // 2. Put the property "isActive" in false.
        existUser.setIsActive(false);

        // 3. Save the changes in DB.
        userRepository.save(existUser);

    }

    public UserDto login(LoginUserRequestDto loginUserRequestDto) {
        User userDB = userRepository.findByUsername(loginUserRequestDto.username())
                .orElseThrow(() -> new ResourceNotFoundException("Username not registered yet"));

        boolean matchPassword = passwordEncoder.matches(loginUserRequestDto.password(), userDB.getPassword());

        if (!matchPassword) {
            throw new RequestRejectedException("Contraseña invalida");
        }

        // 3. Return the mapped data.
        return UserMapper.INSTANCE.toUserDto(userDB);

    }
}
