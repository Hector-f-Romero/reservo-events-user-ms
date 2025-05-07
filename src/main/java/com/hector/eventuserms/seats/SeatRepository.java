package com.hector.eventuserms.seats;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hector.eventuserms.seats.models.Seat;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query(value = """
                  SELECT CASE
                      WHEN COUNT(s) > 0 THEN TRUE
                      ELSE FALSE
                  END
                  FROM seats as s
            WHERE s.id = :seatId AND s.userid IS NULL;
                  """, nativeQuery = true)
    boolean isAvailableToReserve(@Param("seatId") UUID seatId);

    @Query(value = """
            SELECT CASE
                WHEN COUNT(s) > 0 THEN TRUE
                ELSE FALSE
            END
            FROM seats AS s
            WHERE s.eventId = :eventId
            AND s.userId = :userId
            """, nativeQuery = true)
    boolean existsByEventIdAndUserId(@Param("eventId") UUID eventId, @Param("userId") UUID userId);
}
