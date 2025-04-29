package com.hector.eventuserms.seats;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hector.eventuserms.seats.models.Seat;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

}
