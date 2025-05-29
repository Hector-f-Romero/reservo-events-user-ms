package com.hector.eventuserms.users.nats;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import jakarta.annotation.PostConstruct;

@Component
public class UserPublisher {

    private final Connection connection;
    private final ApplicationEventPublisher userPublisher;

    public UserPublisher(Connection connection, ApplicationEventPublisher userPublisher) {
        this.connection = connection;
        this.userPublisher = userPublisher;
    }

    @PostConstruct
    public void initialize() {
        Dispatcher dispatcher = connection.createDispatcher();

        // Register each NATS subject and publish the corresponding event
        dispatcher.subscribe(UserSubjects.GET_ALL,
                (msg) -> this.publishNatsEvent(msg, UserSubjects.GET_ALL));

        dispatcher.subscribe(UserSubjects.GET_ID,
                (msg) -> this.publishNatsEvent(msg, UserSubjects.GET_ID));

        dispatcher.subscribe(UserSubjects.CREATE,
                (msg) -> this.publishNatsEvent(msg, UserSubjects.CREATE));

        dispatcher.subscribe(UserSubjects.UPDATE,
                (msg) -> this.publishNatsEvent(msg, UserSubjects.UPDATE));

        dispatcher.subscribe(UserSubjects.DELETE,
                (msg) -> this.publishNatsEvent(msg, UserSubjects.DELETE));

        dispatcher.subscribe(UserSubjects.LOGIN,
                (msg) -> this.publishNatsEvent(msg, UserSubjects.LOGIN));

    }

    // Creates a custom event with the received NATS message and publishes it using
    // Spring's event publisher.
    private void publishNatsEvent(Message msg, String subject) {
        UserNatsMessage newNatsMessage = new UserNatsMessage(msg, subject);
        this.userPublisher.publishEvent(newNatsMessage);
    }

}
