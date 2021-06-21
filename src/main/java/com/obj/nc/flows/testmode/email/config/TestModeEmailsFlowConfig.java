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

import lombok.extern.log4j.Log4j2;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
@Log4j2
public class TestModeEmailsFlowConfig {
	 
	@Autowired private TestModeProperties testModeProps;
	@Autowired private GreenMailReceiverSourceSupplier greenMailMessageSource;
    
	public final static String TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME = "tmGMSource";
	public final static String TEST_MODE_SOURCE_TRIGGER_BEAN_NAME = "tmSourceTrigger";
	
    @Bean
    @DependsOn(TestModeFlowConfig.TEST_MODE_AGGREGATE_AND_SEND_FLOW_NAME)
    public IntegrationFlow testModeProcessRecievedEmailMessage() {
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
