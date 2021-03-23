package com.obj.nc.flows.testmode.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

import com.obj.nc.flows.testmode.functions.sources.GreenMailReceiverSourceSupplier;
import com.obj.nc.functions.processors.messageAggregator.MessageAggregatorProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;

import lombok.AllArgsConstructor;

@Configuration
@ConditionalOnProperty(value = "nc.flows.test-mode.enabled", havingValue = "true")
public class TestModeFlowConfig {
	
	@Autowired private TestModeProperties testModeProps;
	@Autowired private GreenMailReceiverSourceSupplier greenMailMessageSource;
	@Autowired private MessageAggregatorProcessingFunction messageAggregator;
    
    @Qualifier(TestModeBeansConfig.TEST_MODE_EMAIL_SENDER_FUNCTION_BEAN_NAME)
    @Autowired private EmailSender sendEmailRealSmtp;
    @Autowired private PaylaodLoggerSinkConsumer logConsumer;
	
	public final static String TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME = "greenMailSource";

    @Bean
    public IntegrationFlow testModeSendMessage() {
        return IntegrationFlows.from(greenMailMessageSource,
                        config -> config.poller(testModeSourcePoller()).id(TEST_MODE_GREEN_MAIL_SOURCE_BEAN_NAME))
                .transform(messageAggregator)
                .transform(sendEmailRealSmtp)
                .handle(logConsumer).get();
    }

    @Bean
    public Trigger testModeSourceTrigger() {
        return new PeriodicTrigger(testModeProps.getPeriodInSeconds(), TimeUnit.SECONDS);
    }

    @Bean
    public PollerSpec testModeSourcePoller() {
        return Pollers.trigger(testModeSourceTrigger());
    }

}
