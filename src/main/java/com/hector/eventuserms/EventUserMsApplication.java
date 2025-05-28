package com.hector.eventuserms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication()
@EnableAspectJAutoProxy
public class EventUserMsApplication {

	public static void main(String[] args) {

		// 1. Load enviroment variables
		Dotenv dotenv = Dotenv.configure().load();

		// 2. Convert .env variables to env system vars. For this,
		// application.properties can use it .env file.
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(EventUserMsApplication.class, args);

	}

}
