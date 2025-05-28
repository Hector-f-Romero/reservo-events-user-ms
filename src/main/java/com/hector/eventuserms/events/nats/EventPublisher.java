package com.hector.eventuserms.events.nats;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Component
public class EventPublisher {

    private final Connection natsConnection;
    private final ApplicationEventPublisher eventPublisher;

    public EventPublisher(Connection natsConnection, ApplicationEventPublisher eventPublisher) {
        this.natsConnection = natsConnection;
        this.eventPublisher = eventPublisher;
    }

    /*
     * If we want to use AOP to intercept errors that may occur during
     * business logic or serialization while handling NATS messages,
     * we can't use callbacks directly, as they bypass the Spring proxy.
     *
     * Instead, we publish Spring custom events here,
     * allowing methods annotated with @EventListener and @NatsHandler
     * to be intercepted by aspects that centralize error handling.
     *
     * The logic is split into three components:
     * - EventPublisher: publishes the events.
     * - EventNatsController: listens to the events and executes logic.
     * - EventSubjects: holds the NATS subject names (we avoid enums for easier
     * conditional arg used in @EventListener).
     */
    @PostConstruct
    public void initialize() {
        Dispatcher dispatcher = natsConnection.createDispatcher();

        // Register each NATS subject and publish the corresponding event
        dispatcher.subscribe(EventSubjects.GET_ID,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.GET_ID));

        dispatcher.subscribe(EventSubjects.GET_ALL_EVENTS,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.GET_ALL_EVENTS));

        dispatcher.subscribe(EventSubjects.GET_UPCOMING_EVENTS,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.GET_UPCOMING_EVENTS));

        dispatcher.subscribe(EventSubjects.GET_UPCOMING_EVENTS_BY_DATE,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.GET_UPCOMING_EVENTS_BY_DATE));

        dispatcher.subscribe(EventSubjects.CREATE,
                (msg) -> this.publishNatsEvent(msg, EventSubjects.CREATE));

    }

    // Creates a custom event with the received NATS message and publishes it using
    // Spring's event publisher.
    private void publishNatsEvent(Message msg, String subject) {
        NatsMessageEvent newNatsMessage = new NatsMessageEvent(msg, subject);
        this.eventPublisher.publishEvent(newNatsMessage);
    }
}
