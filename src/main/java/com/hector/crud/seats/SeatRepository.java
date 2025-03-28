package com.hector.crud.seats;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hector.crud.seats.models.Seat;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

}
