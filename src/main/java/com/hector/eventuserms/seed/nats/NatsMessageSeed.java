package com.hector.eventuserms.seed.nats;

import com.hector.eventuserms.common.nats.NatsMessage;

import io.nats.client.Message;

public class NatsMessageSeed extends NatsMessage {

    public NatsMessageSeed(Message msg, String subject) {
        super(msg, subject);
    }
}
