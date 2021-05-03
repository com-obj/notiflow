package com.obj.nc;

import javax.annotation.PostConstruct;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.utility.TestcontainersConfiguration;

@Configuration
@Profile("testcontainers")
public class TestContainersConfig {

    @PostConstruct
    void start() {
        TestcontainersConfiguration.getInstance().updateUserConfig("docker.client.strategy", "org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy");
        TestcontainersConfiguration.getInstance().updateUserConfig("testcontainers.reuse.enable", "true");
        TestcontainersConfiguration.getInstance().updateUserConfig("checks.disable", "true");

        BaseIntegrationTest.POSTGRESQL_CONTAINER = new FixedPortPostgreSQLContainer<>("postgres:11.9", 25432)
                .withPostgreSQLUsername("nc")
                .withPostgreSQLPassword("ZMss4o9mdBLV")
                .withPostgreSQLDatabaseName("nc")
                .withReuse(true);

        if (!BaseIntegrationTest.POSTGRESQL_CONTAINER.isRunning()) {
            BaseIntegrationTest.POSTGRESQL_CONTAINER.start();
            BaseIntegrationTest.POSTGRESQL_CONTAINER.waitForContainer();
        }

        Flyway flyway = Flyway.configure()
                .dataSource(
                        BaseIntegrationTest.POSTGRESQL_CONTAINER.getJdbcUrl(),
                        BaseIntegrationTest.POSTGRESQL_CONTAINER.getUsername(),
                        BaseIntegrationTest.POSTGRESQL_CONTAINER.getPassword())
                .locations("classpath:db/migration")
                .connectRetries(10).load();

        flyway.migrate();
    }

}