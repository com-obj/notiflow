package com.obj.nc;

import org.springframework.boot.test.context.SpringBootTest;

/*
    SINGLETON pattern class with containers for all test classes
    see more: https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
 */
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
public abstract class BaseIntegrationTest {

    static FixedPortPostgreSQLContainer<?> POSTGRESQL_CONTAINER;

}
