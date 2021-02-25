package com.obj.nc;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;

import java.io.File;

/*
    SINGLETON pattern class with containers for all test classes
    see more: https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
 */
public abstract class BaseIntegrationTest {

    public static void initComposeContainer(String dockerComposePath, String... servicesToWaitFor) {
        DockerComposeContainer<?> container = new DockerComposeContainer<>(new File(dockerComposePath))
                .withLocalCompose(true);

        for (String service : servicesToWaitFor) {
            container = container.waitingFor(service, new HostPortWaitStrategy());
        }

        container.start();
    }

}
