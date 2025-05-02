package com.hector.eventuserms.config;

import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

@Configuration
public class NatsConfig {

    private final String natsUri;

    public NatsConfig(@Value("${nats.server}") String natsUri) {
        this.natsUri = natsUri;
    }

    @Bean
    public Connection natsConnection() throws IOException, InterruptedException {
        Options options = new Options.Builder()
                .server(natsUri)
                .reconnectWait(Duration.ofSeconds(5))
                .maxReconnects(-1)
                .build();

        return Nats.connect(options);
    }

}
