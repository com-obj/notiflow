package com.obj.nc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class FunctionsTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunctionsTestApplication.class, args);
    }

}
