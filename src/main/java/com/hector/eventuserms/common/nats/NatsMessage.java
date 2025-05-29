package com.hector.eventuserms.common.nats;

import io.nats.client.Message;

public abstract class NatsMessage {
    public final Message msg;
    public final String subject;

    public NatsMessage(Message msg, String subject) {
        this.msg = msg;
        this.subject = subject;
    }

}
