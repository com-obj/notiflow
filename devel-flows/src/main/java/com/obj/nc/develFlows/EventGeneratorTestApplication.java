package com.obj.nc.develFlows;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class EventGeneratorTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventGeneratorTestApplication.class, args);
	}

}
