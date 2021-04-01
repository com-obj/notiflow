package com.obj.nc.flows.testmode.sms.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.config.TestModeFlowConfig;
import com.obj.nc.flows.testmode.sms.funcitons.sources.InMemorySmsSourceSupplier;
import com.obj.nc.functions.processors.senders.SmsSender;

import lombok.extern.log4j.Log4j2;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
@ConditionalOnBean(SmsSender.class)
@Log4j2
public class TestModeSmsFlowConfig {
	
	@Autowired private TestModeProperties testModeProps;
	@Autowired private InMemorySmsSourceSupplier inMemorySmsSource;
	
	public final static String TEST_MODE_SMS_SOURCE_BEAN_NAME = "tmSmsInMemorySource";
	public final static String TEST_MODE_SMS_SOURCE_TRIGGER_BEAN_NAME = "tmSmsSourceTrigger";

    @Bean
    public IntegrationFlow testModeProcessRecievedSmsMessage() {
        return IntegrationFlows
        		.fromSupplier(inMemorySmsSource,
                      config -> config.poller(Pollers.trigger(testModeSourceTrigger()))
                      .id(TEST_MODE_SMS_SOURCE_BEAN_NAME))
        		.split()
        		.channel(TestModeFlowConfig.TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME)
        		.get();

    }
    
    @Bean(TEST_MODE_SMS_SOURCE_TRIGGER_BEAN_NAME)
    public Trigger testModeSourceTrigger() {
        return new PeriodicTrigger(testModeProps.getPollMockSourcesPeriodInSeconds(), TimeUnit.SECONDS);
    }

}
