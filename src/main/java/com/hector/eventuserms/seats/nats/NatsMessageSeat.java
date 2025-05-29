package com.hector.eventuserms.seats.nats;

import com.hector.eventuserms.common.nats.NatsMessage;

import io.nats.client.Message;

public class NatsMessageSeat extends NatsMessage {

        public NatsMessageSeat(Message msg, String subject) {
                super(msg, subject);
        }

}
