package com.bdc.fullstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DesafioFullstackApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesafioFullstackApplication.class, args);
	}
	
}
