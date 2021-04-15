package com.obj.nc.flows.testmode.mailchimp.config;

import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.flows.testmode.config.TestModeFlowConfig;
import com.obj.nc.flows.testmode.mailchimp.functions.InMemoryMailchimpSourceSupplier;
import com.obj.nc.functions.processors.senders.SmsSender;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
@ConditionalOnBean(SmsSender.class)
@Log4j2
public class TestModeMailchimpFlowConfig {
	
	@Autowired private TestModeProperties testModeProps;
	@Autowired private InMemoryMailchimpSourceSupplier inMemoryMailchimpSource;
	
	public final static String TEST_MODE_MAILCHIMP_SOURCE_BEAN_NAME = "tmMailchimpInMemorySource";
	public final static String TEST_MODE_MAILCHIMP_SOURCE_TRIGGER_BEAN_NAME = "tmMailchimpSourceTrigger";

    @Bean
    @DependsOn(TestModeFlowConfig.TEST_MODE_AGGREGATE_AND_SEND_FLOW_NAME)
    public IntegrationFlow testModeProcessRecievedMailchimpMessage() {
        return IntegrationFlows
        		.fromSupplier(inMemoryMailchimpSource,
                      config -> config.poller(Pollers.trigger(testModeSourceTrigger()))
                      .id(TEST_MODE_MAILCHIMP_SOURCE_BEAN_NAME))
        		.split()
        		.channel(TestModeFlowConfig.TEST_MODE_THREAD_EXECUTOR_CHANNEL_NAME)
        		.get();

    }
    
    @Bean(TEST_MODE_MAILCHIMP_SOURCE_TRIGGER_BEAN_NAME)
    public Trigger testModeSourceTrigger() {
        return new PeriodicTrigger(testModeProps.getPollMockSourcesPeriodInSeconds(), TimeUnit.SECONDS);
    }

}
