package com.obj.nc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "testContainers", havingValue = "local", matchIfMissing = true)
public class LocalTestContainerConfigurer {

    public static final String DOCKER_COMPOSE_PATH = "../docker/koderia-flows/test-components/docker-compose.yml";

    @Bean
    @Primary
    public FlywayMigrationStrategy flywayMigrationStrategyLocal() {
        return flyway -> {
            LocalTestContainers.initContainers(DOCKER_COMPOSE_PATH, "nc-postgres");

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            flyway.migrate();
        };
    }

}
