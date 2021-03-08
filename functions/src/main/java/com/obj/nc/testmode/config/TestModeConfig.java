package com.obj.nc.testmode.config;

import com.obj.nc.functions.processors.messageAggregator.MessageAggregatorMicroService;
import com.obj.nc.functions.processors.senders.EmailSenderSinkMicroService;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerMicroService;
import com.obj.nc.testmode.functions.sources.GreenMailReceiverMicroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;

import java.util.concurrent.TimeUnit;

import static com.obj.nc.testmode.functions.processors.TestModeEmailSenderConfig.TEST_MODE_EMAIL_SENDER_MICRO_SERVICE;

@Configuration
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class TestModeConfig {

    @Autowired
    private GreenMailReceiverMicroService greenMailSource;

    @Autowired
    private MessageAggregatorMicroService aggregator;

    @Autowired
    @Qualifier(TEST_MODE_EMAIL_SENDER_MICRO_SERVICE)
    private EmailSenderSinkMicroService testmodeSendEmail;

    @Autowired
    private PaylaodLoggerMicroService logger;

    @Bean
    public IntegrationFlow testModeSendMessage() {
        return IntegrationFlows
                .from(greenMailSource, config -> config.poller(Pollers.fixedDelay(10000)))
                .transform(aggregator)
                .transform(testmodeSendEmail)
                .handle(logger)
                .get();
    }

}
