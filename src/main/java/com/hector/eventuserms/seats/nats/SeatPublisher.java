package com.hector.eventuserms.seats.nats;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Component
public class SeatPublisher {

    private final Connection connection;
    private final ApplicationEventPublisher seatPublisher;

    public SeatPublisher(Connection connection, ApplicationEventPublisher eventPublisher) {
        this.connection = connection;
        this.seatPublisher = eventPublisher;
    }

    /*
     * If we want to use AOP to intercept errors that may occur during
     * business logic or serialization while handling NATS messages,
     * we can't use callbacks directly, as they bypass the Spring proxy.
     *
     * Instead, we publish Spring custom events here,
     * allowing methods annotated with @EventListener and @NatsHandler
     * to be intercepted by aspects that centralize error handling.
     */
    @PostConstruct
    public void initialize() {
        Dispatcher dispatcher = this.connection.createDispatcher();

        // Register each NATS subject and publish the corresponding event
        dispatcher.subscribe(SeatSubjects.GET_ID,
                (msg) -> this.publishNatsEvent(msg, SeatSubjects.GET_ID));

        dispatcher.subscribe(SeatSubjects.CREATE,
                (msg) -> this.publishNatsEvent(msg, SeatSubjects.CREATE));

        dispatcher.subscribe(SeatSubjects.CREATE_ALL,
                (msg) -> this.publishNatsEvent(msg, SeatSubjects.CREATE_ALL));

        dispatcher.subscribe(SeatSubjects.UPDATE,
                (msg) -> this.publishNatsEvent(msg, SeatSubjects.UPDATE));
    }

    // Creates a custom event with the received NATS message and publishes it using
    // Spring's event publisher.
    private void publishNatsEvent(Message msg, String subject) {
        NatsMessageSeat newNatsMessage = new NatsMessageSeat(msg, subject);
        this.seatPublisher.publishEvent(newNatsMessage);
    }
}
