package com.obj.nc;

import org.flywaydb.core.Flyway;

/*
    SINGLETON pattern class with containers for all test classes
    see more: https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
 */
public class JenkinsTestContainers {

    public static PostgreSQLPreMadeContainer POSTGRESQL_CONTAINER;

    public static void initContainers(String dockerImage, String userName, String password, String databaseName, int hostPort) {
        PostgreSQLPreMadeContainer postgreSQLPreMadeContainer = new PostgreSQLPreMadeContainer(dockerImage)
                .withPostgreSQLUsername(userName)
                .withPostgreSQLPassword(password)
                .withPostgreSQLDatabaseName(databaseName)
                .waitForContainer();

        postgreSQLPreMadeContainer.initialize();
        POSTGRESQL_CONTAINER = postgreSQLPreMadeContainer;

        System.setProperty("spring.datasource.url", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("spring.datasource.username", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("spring.datasource.password", POSTGRESQL_CONTAINER.getPassword());
    }

    public static Flyway getFlyway() {
        return Flyway.configure().dataSource(POSTGRESQL_CONTAINER.getJdbcUrl(), POSTGRESQL_CONTAINER.getUsername(), POSTGRESQL_CONTAINER.getPassword())
                .locations("classpath:db/migration")
                .load();
    }

}
