package com.obj.nc;

import javax.annotation.PostConstruct;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "testEnv", havingValue = "jenkins")
public class JenkinsTestContainersConfig {

    @PostConstruct
    void start() {
        BaseIntegrationTest.POSTGRESQL_CONTAINER = new FixedPortPostgreSQLContainer<>("postgres:11.9", 5432)
                .withPostgreSQLUsername("nc")
                .withPostgreSQLPassword("ZMss4o9mdBLV")
                .withPostgreSQLDatabaseName("nc");

        BaseIntegrationTest.POSTGRESQL_CONTAINER.waitForContainer();

        Flyway flyway = Flyway.configure()
                .dataSource(
                        BaseIntegrationTest.POSTGRESQL_CONTAINER.getJdbcUrl(),
                        BaseIntegrationTest.POSTGRESQL_CONTAINER.getUsername(),
                        BaseIntegrationTest.POSTGRESQL_CONTAINER.getPassword())
                .locations("classpath:db/migration")
                .load();

        flyway.migrate();
    }

}
