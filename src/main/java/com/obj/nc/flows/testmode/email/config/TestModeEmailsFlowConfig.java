/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.flows.testmode.email.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.config.TestModeFlowConfig;
import com.obj.nc.flows.testmode.email.functions.sources.GreenMailReceiverSourceSupplier;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class TestModeEmailsFlowConfig {
	 
	@Autowired private TestModeProperties testModeProps;
	@Autowired private GreenMailReceiverSourceSupplier greenMailMessageSource;
    
	public final static String TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME = "tmGMSource";
	public final static String TEST_MODE_SOURCE_TRIGGER_BEAN_NAME = "tmSourceTrigger";
	
    @Bean
    @DependsOn(TestModeFlowConfig.TEST_MODE_AGGREGATE_AND_SEND_FLOW_NAME)
    public IntegrationFlow testModeProcessReceivedEmailMessage() {
        return IntegrationFlows
        		.fromSupplier(greenMailMessageSource,
                      config -> config.poller(Pollers.trigger(testModeSourceTrigger()))
                      .id(TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME))
        		.split()
        		.channel(TestModeFlowConfig.TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME).get();
    }
    


    @Bean(TEST_MODE_SOURCE_TRIGGER_BEAN_NAME)
    public Trigger testModeSourceTrigger() {
        return new PeriodicTrigger(testModeProps.getPollMockSourcesPeriodInSeconds(), TimeUnit.SECONDS);
    }


}
