/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.dataSources;

import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.inpuEventRouting.ExtensionBasedEventConverterTests;
import com.obj.nc.flows.inputEventRouting.extensions.InputEvent2IntentConverterExtension;
import com.obj.nc.flows.inputEventRouting.extensions.InputEvent2MessageConverterExtension;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.testUtils.BaseIntegrationTest;
import com.obj.nc.testUtils.SystemPropertyActiveProfileResolver;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rnorth.ducttape.TimeoutException;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.obj.nc.flows.inputEventRouting.config.InputEventRoutingFlowConfig.GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(value = "test", resolver = SystemPropertyActiveProfileResolver.class)
@TestPropertySource(locations = "/data-sources-test.properties")
@SpringIntegrationTest(noAutoStartup = {
        "NC_CUSTOM_DATA_SOURCE_first-jdbc.payload-regular_INTEGRATION_FLOW_POLLER"
})
@SpringBootTest
public class DataSourcesConfigurationTest extends BaseIntegrationTest {
    
    @Qualifier("NC_CUSTOM_DATA_SOURCE_first-jdbc.payload-regular_INTEGRATION_FLOW_POLLER")
    @Autowired private SourcePollingChannelAdapter jdbcPollableSource;
    @Autowired private GenericEventRepository genericEventRepository;
    
    @Container
    private final FixedPortPostgreSQLContainer<?> postgresqlContainer = new FixedPortPostgreSQLContainer<>("postgres:11.9", 27305)
            .withPostgreSQLInitScript("src/test/resources/dataSources/init.sql")
            .withPostgreSQLDatabaseName("ds")
            .withPostgreSQLUsername("ds")
            .withPostgreSQLPassword("ds");
    
    @BeforeEach
    void setUp(@Autowired JdbcTemplate jdbcTemplate) {
        jdbcPollableSource.stop();
        
        purgeNotifTables(jdbcTemplate);
        
        Awaitility
                .await()
                .atMost(3, TimeUnit.SECONDS)
                .until(() -> genericEventRepository.count() >= 1);
        
    }
    
    @AfterEach
    public void stopSourcePolling() {
        jdbcPollableSource.stop();
    }
    
    @Test
    void testJobsCreated() {

    }
    
//    @TestConfiguration
//    public static class EventConvertionExtensionConfiguration {
//        
//        @Bean
//        public InputEvent2MessageConverterExtension event2Message() {
//            return new InputEvent2MessageConverterExtension () {
//                @Override
//                public String getFlowId() {
//                    return "first-jdbc.payload-regular";
//                }
//    
//                @Override
//                public List<Message<?>> convertEvent(GenericEvent event) {
//                    
//                    
//                    TemplateWithModelEmailContent
//                    
//                    List<Message<?>> msg = Arrays.asList(email1);
//                    
//                    return msg;
//                }
//            };
//        }
//    }
    
    public static class FixedPortPostgreSQLContainer<SELF extends FixedPortPostgreSQLContainer<SELF>> extends PostgreSQLContainer<SELF> {
        
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
    
}
