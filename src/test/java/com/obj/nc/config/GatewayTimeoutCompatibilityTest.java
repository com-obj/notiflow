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

package com.obj.nc.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.gateway.GatewayProxyFactoryBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(GatewayTimeoutCompatibilityTest.Config.class)
class GatewayTimeoutCompatibilityTest {
    @Autowired private SlowGateway gateway;
    @Autowired private QueueChannel requests;

    @Test
    void asyncGatewaysStillAcceptRepliesAfterTheNewThirtySecondDefault() throws Exception {
        Future<String> result = gateway.send("notification");
        try {
            Message<?> request = requests.receive(5000);
            assertThat(request).isNotNull();
            TimeUnit.SECONDS.sleep(31);
            assertThat(result.isDone()).isFalse();
            MessageChannel reply = (MessageChannel) request.getHeaders().get(MessageHeaders.REPLY_CHANNEL);
            assertThat(reply.send(new GenericMessage<>("completed"))).isTrue();
            assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("completed");
        } finally {
            result.cancel(true);
        }
    }

    interface SlowGateway {
        Future<String> send(String payload);
    }

    @Configuration
    @EnableIntegration
    static class Config {
        @Bean
        QueueChannel requests() {
            return new QueueChannel();
        }

        @Bean
        GatewayProxyFactoryBean slowGateway(QueueChannel requests) {
            var factory = new GatewayProxyFactoryBean(SlowGateway.class);
            factory.setDefaultRequestChannel(requests);
            return factory;
        }
    }
}
