package com.hector.eventuserms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication()
public class EventUserMsApplication {

	public static void main(String[] args) {

		// Load enviroment variables
		Dotenv dotenv = Dotenv.configure().load();

		System.setProperty("spring.datasource.url", dotenv.get("DATABASE_URL"));
		System.setProperty("spring.datasource.username",
				dotenv.get("DATABASE_USERNAME"));
		System.setProperty("spring.datasource.password",
				dotenv.get("DATABASE_PASSWORD"));
		System.setProperty("nats.server", dotenv.get("NATS_SERVER"));

		SpringApplication.run(EventUserMsApplication.class, args);

	}

}
