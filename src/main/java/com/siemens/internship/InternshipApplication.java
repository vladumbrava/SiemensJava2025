package com.siemens.internship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Since we have a method annotated with @Async in our service, we firstly need to
 * enable asynchronous processing in our Spring Boot application. We do so, by
 * annotating our application class with @EnableAsync
 */

@EnableAsync
@SpringBootApplication
public class InternshipApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternshipApplication.class, args);
	}

}
