package com.obj.nc.osk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.obj.nc") 
@EnableIntegration
@EnableTransactionManagement
public class OskFlowsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OskFlowsApplication.class, args);
    }

    
}
