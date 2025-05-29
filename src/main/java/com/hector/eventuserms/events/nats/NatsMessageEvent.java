package com.hector.eventuserms.events.nats;

import com.hector.eventuserms.common.nats.NatsMessage;

import io.nats.client.Message;

public class NatsMessageEvent extends NatsMessage {

        public NatsMessageEvent(Message msg, String subject) {
                super(msg, subject);
        }

}
