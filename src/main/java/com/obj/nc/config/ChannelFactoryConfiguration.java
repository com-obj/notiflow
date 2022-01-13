/*
 * Copyright (C) 2021 the original author or authors.
 * This file is part of Notiflow
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.obj.nc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.channels.ChannelFactory;
import com.obj.nc.channels.RabbitmqChannelFactory;
import com.obj.nc.channels.SpringChannelFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ChannelFactoryConfiguration {
    @Bean
    @ConditionalOnProperty(prefix = "nc.channel", name = "factory", havingValue = "rabbitmq")
    public ChannelFactory rabbitmqChannelFactory(final ConnectionFactory connectionFactory, final ObjectMapper objectMapper) {
        return new RabbitmqChannelFactory(connectionFactory, objectMapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "nc.channel", name = "factory", havingValue = "spring", matchIfMissing = true)
    public ChannelFactory springChannelFactory(final ThreadPoolTaskScheduler executor) {
        return new SpringChannelFactory(executor);
    }
}
