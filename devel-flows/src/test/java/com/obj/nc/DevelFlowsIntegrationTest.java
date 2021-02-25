package com.obj.nc;

public abstract class DevelFlowsIntegrationTest extends BaseIntegrationTest {

    public static final String DOCKER_COMPOSE_PATH = "../docker/devel-flows/test-components/docker-compose.yml";

    static {
        initComposeContainer(DOCKER_COMPOSE_PATH, "nc-postgres");
    }

}
