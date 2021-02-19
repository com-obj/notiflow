package com.obj.nc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class OrangeFlowsApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrangeFlowsApplication.class, args);
    }

}
