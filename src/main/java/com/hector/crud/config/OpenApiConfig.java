package com.hector.crud.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Event-user service", version = "1.0.0", description = "This microservice provides a REST API for performing CRUD operations on core entities: 'users', 'events', and 'seats'."))
public class OpenApiConfig {

}
