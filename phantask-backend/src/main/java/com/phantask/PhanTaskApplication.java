package com.phantask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PhanTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhanTaskApplication.class, args);
	}

}
