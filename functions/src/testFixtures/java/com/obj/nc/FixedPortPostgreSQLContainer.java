package com.obj.nc;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.rnorth.ducttape.TimeoutException;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.PostgreSQLContainer;

public class FixedPortPostgreSQLContainer<SELF extends FixedPortPostgreSQLContainer<SELF>> extends PostgreSQLContainer<SELF> {

    private final String dockerImageName;

    private final int fixedHostPort;

    public FixedPortPostgreSQLContainer(String dockerImage, int hostPort) {
        super(dockerImage);
        this.dockerImageName = dockerImage;
        this.fixedHostPort = hostPort;
        super.addFixedExposedPort(fixedHostPort, POSTGRESQL_PORT);
        super.setWaitStrategy(new org.testcontainers.containers.wait.strategy.AbstractWaitStrategy() {
            @Override
            protected void waitUntilReady() {
                try {
                    Unreliables.retryUntilTrue((int) startupTimeout.getSeconds(), TimeUnit.SECONDS,
                            () -> getRateLimiter().getWhenReady(() -> {
                                try {
                                    new Socket("localhost", fixedHostPort).close();
                                } catch (IOException e) {
                                    throw new IllegalStateException("Socket not listening yet: " + fixedHostPort);
                                }
                                return true;
                            }));

                } catch (TimeoutException e) {
                    throw new ContainerLaunchException("Timed out waiting for container port to open (" +
                            "localhost" +
                            " ports: " +
                            fixedHostPort +
                            " should be listening)");
                }
            }
        });
    }

    public FixedPortPostgreSQLContainer<SELF> withPostgreSQLInitScript(String initScriptPath) {
        super.withInitScript(initScriptPath);
        return this;
    }

    public FixedPortPostgreSQLContainer<SELF> withPostgreSQLUsername(String username) {
        super.withUsername(username);
        return this;
    }

    public FixedPortPostgreSQLContainer<SELF> withPostgreSQLPassword(String password) {
        super.withPassword(password);
        return this;
    }

    public FixedPortPostgreSQLContainer<SELF> withPostgreSQLDatabaseName(String databaseName) {
        super.withDatabaseName(databaseName);
        return this;
    }

    public void waitForContainer() {
        super.waitUntilContainerStarted();
    }

    public void initialize() {
        super.runInitScriptIfRequired();
    }

    @Override
    public boolean isRunning() {
        try {
            new Socket("localhost", fixedHostPort).close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    @Override
    public String getContainerId() {
        return "K8S:Postgres";
    }

    @Override
    public String getDockerImageName() {
        return dockerImageName;
    }

    @Override
    public String getJdbcUrl() {
        // Disable Postgres driver use of java.util.logging to reduce noise at startup time
        return "jdbc:postgresql://" + "localhost" + ":" + fixedHostPort + "/" + getDatabaseName() + "?loggerLevel=OFF";
    }

}

