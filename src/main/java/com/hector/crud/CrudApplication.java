package com.hector.crud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
// @ComponentScan(basePackages = {
// "com.hector.crud.config",
// "com.hector.crud.users",
// "com.hector.crud.events",
// "com.hector.crud.exception",
// "com.hector.crud.seats"
// })
public class CrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrudApplication.class, args);
	}

}
