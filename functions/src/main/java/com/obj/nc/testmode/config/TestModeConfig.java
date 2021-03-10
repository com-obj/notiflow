package com.obj.nc.testmode.config;

import com.obj.nc.functions.processors.messageAggregator.MessageAggregatorProcessingFunction;
import com.obj.nc.functions.processors.senders.EmailSenderSinkProcessingFunction;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import com.obj.nc.testmode.functions.processors.TestModeEmailSenderProperties;
import com.obj.nc.testmode.functions.sources.GreenMailReceiverSourceSupplier;
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

import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class TestModeConfig {

    @Autowired
    private TestModeEmailSenderProperties properties;

    @Autowired
    private GreenMailReceiverSourceSupplier greenMailMessageSource;

    @Autowired
    private MessageAggregatorProcessingFunction messageAggregator;

    @Autowired
    @Qualifier("testModeEmailSenderSinkProcessingFunction")
    private EmailSenderSinkProcessingFunction testModeSendEmailProcessingFunction;

    @Autowired
	private PaylaodLoggerSinkConsumer logConsumer;

    @Bean
    public IntegrationFlow testModeSendMessage() {
        return IntegrationFlows.from(greenMailMessageSource,
                        config -> config.poller(sourcePoller()).id("greenMailSource"))
                .transform(messageAggregator)
                .transform(testModeSendEmailProcessingFunction)
                .handle(logConsumer).get();
    }

    @Bean
    public Trigger sourceTrigger() {
        return new PeriodicTrigger(properties.getPeriodMinutes(), TimeUnit.MINUTES);
    }

    @Bean
    public PollerSpec sourcePoller() {
        return Pollers.trigger(sourceTrigger());
    }

}
