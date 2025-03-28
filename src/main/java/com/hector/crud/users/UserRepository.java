package com.hector.crud.users;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hector.crud.users.models.User;

public interface UserRepository extends JpaRepository<User, UUID> {

}
