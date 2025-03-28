package com.hector.crud.events;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hector.crud.events.models.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

}
