package com.obj.nc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.obj.nc")
public class EventGeneratorTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventGeneratorTestApplication.class, args);
	}

}
