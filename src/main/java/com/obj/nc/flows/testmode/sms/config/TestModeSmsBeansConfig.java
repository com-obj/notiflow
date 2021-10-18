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

package com.obj.nc.flows.testmode.sms.config;

import com.obj.nc.flows.testmode.sms.funcitons.processors.InMemorySmsSender;
import com.obj.nc.flows.testmode.sms.funcitons.sources.InMemorySmsSourceSupplier;
import com.obj.nc.functions.processors.senders.SmsSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class TestModeSmsBeansConfig {
	
    /**
     * The provided smsSender service override. Instead of performing real SMS delivery, this implementation sends sms
     * to in memory store which is then used to create digest email
     * @param properties
     * @return
     */
    @Bean
    @Primary
    public SmsSender smsSender() {
        return new InMemorySmsSender(smsReciever());
    }
    
    @Bean
    @Primary
    public InMemorySmsSourceSupplier smsReciever() {
        return new InMemorySmsSourceSupplier();

    }

    
}
