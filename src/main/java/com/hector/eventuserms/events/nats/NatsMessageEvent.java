package com.hector.eventuserms.events.nats;

import io.nats.client.Message;

public record NatsMessageEvent(
        Message msg,
        String subject) {

}
