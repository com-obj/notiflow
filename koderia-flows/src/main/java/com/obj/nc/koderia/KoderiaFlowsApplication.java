package com.obj.nc.koderia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class KoderiaFlowsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KoderiaFlowsApplication.class, args);
    }

}
