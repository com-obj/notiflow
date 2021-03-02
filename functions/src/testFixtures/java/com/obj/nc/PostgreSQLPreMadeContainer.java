package com.obj.nc;

import org.rnorth.ducttape.TimeoutException;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * We are expecting that postgresql container already will be created by an external system.
 * Also this class is able to wait for 60 seconds until postgresql container starts.
 */
public class PostgreSQLPreMadeContainer extends PostgreSQLContainer {

    private final String dockerImageName;

    public PostgreSQLPreMadeContainer(String dockerImage) {
        super(dockerImage);
        this.dockerImageName = dockerImage;
    }

    public PostgreSQLPreMadeContainer withPostgreSQLInitScript(String initScriptPath) {
        super.withInitScript(initScriptPath);
        return this;
    }

    public PostgreSQLPreMadeContainer withPostgreSQLUsername(String username) {
        super.withUsername(username);
        return this;
    }

    public PostgreSQLPreMadeContainer withPostgreSQLPassword(String password) {
        super.withPassword(password);
        return this;
    }

    public PostgreSQLPreMadeContainer withPostgreSQLDatabaseName(String databaseName) {
        super.withDatabaseName(databaseName);
        return this;
    }

    public PostgreSQLPreMadeContainer waitForContainer() {
        super.setWaitStrategy(new org.testcontainers.containers.wait.strategy.AbstractWaitStrategy() {
            @Override
            protected void waitUntilReady() {
                try {
                    Unreliables.retryUntilTrue((int) startupTimeout.getSeconds(), TimeUnit.SECONDS,
                            () -> getRateLimiter().getWhenReady(() -> {
                                try {
                                    new Socket("localhost", POSTGRESQL_PORT).close();
                                } catch (IOException e) {
                                    throw new IllegalStateException("Socket not listening yet: " + POSTGRESQL_PORT);
                                }
                                return true;
                            }));

                } catch (TimeoutException e) {
                    throw new ContainerLaunchException("Timed out waiting for container port to open (" +
                            "localhost" +
                            " ports: " +
                            POSTGRESQL_PORT +
                            " should be listening)");
                }
            }
        });
        super.waitUntilContainerStarted();
        return this;
    }

    public void initialize() {
        super.runInitScriptIfRequired();
    }

    @Override
    public boolean isRunning() {
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
        return "jdbc:postgresql://" + "localhost" + ":" + POSTGRESQL_PORT + "/" + getDatabaseName() + "?loggerLevel=OFF";
    }
}

