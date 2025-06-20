package com.hector.eventuserms.seed.nats;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Component
public class SeedPublisher {

    private final Connection connection;
    private final ApplicationEventPublisher seedPublisher;

    public SeedPublisher(Connection connection, ApplicationEventPublisher seedPublisher) {
        this.connection = connection;
        this.seedPublisher = seedPublisher;
    }

    @PostConstruct
    public void initialize() {

        Dispatcher dispatcher = this.connection.createDispatcher();

        dispatcher.subscribe(SeedSubjects.EXECUTE_SEED, (msg) -> this.publishNatsEvent(msg, SeedSubjects.EXECUTE_SEED));
    }

    // Creates a custom event with the received NATS message and publishes it using
    // Spring's event publisher.
    private void publishNatsEvent(Message msg, String subject) {
        NatsMessageSeed newNatsMessage = new NatsMessageSeed(msg, subject);
        this.seedPublisher.publishEvent(newNatsMessage);
    }
}
