package com.hector.eventuserms.users.nats;

import com.hector.eventuserms.common.nats.NatsMessage;

import io.nats.client.Message;

public class UserNatsMessage extends NatsMessage {

    public UserNatsMessage(Message msg, String subject) {
        super(msg, subject);
    }

}