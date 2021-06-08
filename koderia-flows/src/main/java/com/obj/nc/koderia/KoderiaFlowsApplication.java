package com.obj.nc.koderia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.obj.nc")
@EnableIntegration
@IntegrationComponentScan(basePackages = "com.obj.nc")
@EnableTransactionManagement
@EnableJdbcRepositories(basePackages = {"com.obj.nc.osk.repositories"})
public class KoderiaFlowsApplication {

    public static void main(String[] args) {
        SpringApplication.run(KoderiaFlowsApplication.class, args);
    }

}
