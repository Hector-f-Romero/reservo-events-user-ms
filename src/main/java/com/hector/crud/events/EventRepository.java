package com.hector.crud.events;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hector.crud.events.models.Event;

public interface EventRepository extends JpaRepository<Event, UUID> {

        @Query(value = """
                        SELECT e.id, e.name, e.description, e.date, e.capacity,
                        CAST(COUNT(s.id) AS smallint) AS occupiedSeats, u.name as organizedBy
                        FROM events e
                        LEFT JOIN seats s ON s.eventId = e.id AND s.state = 'OCCUPIED'
                        LEFT JOIN users u on e.organizedBy = u.id
                        WHERE e.date > :currentDate
                        GROUP BY e.id, e.name, e.description, e.date, e.capacity, u.name
                        ORDER BY e.date ASC;
                        """, nativeQuery = true)
        List<Object[]> findUpcomingEventsWithAvailableSeats(
                        @Param("currentDate") ZonedDateTime currentDate);
}
