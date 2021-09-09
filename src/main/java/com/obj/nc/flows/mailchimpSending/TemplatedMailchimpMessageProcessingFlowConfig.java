package com.obj.nc.flows.mailchimpSending;

import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.senders.mailchimp.TemplatedMailchimpMessageSender;
import com.obj.nc.functions.sink.payloadLogger.PaylaodLoggerSinkConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
public class TemplatedMailchimpMessageProcessingFlowConfig {
    
    public final static String MAILCHIMP_TEMPLATE_PROCESSING_FLOW_ID = "MAILCHIMP_TEMPLATE_PROCESSING_FLOW_ID";
    public final static String MAILCHIMP_TEMPLATE_PROCESSING_FLOW_INPUT_CHANNEL_ID = MAILCHIMP_TEMPLATE_PROCESSING_FLOW_ID + "_INPUT";
    
    private final TemplatedMailchimpMessageSender templatedMailchimpMessageSender;
    private final PaylaodLoggerSinkConsumer logConsumer;
    private final MessagePersister messagePersister;
    private final EndpointPersister endpointPersister;
    private final ThreadPoolTaskScheduler executor;
    
    @Bean(MAILCHIMP_TEMPLATE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel mailchimpTemplateProcessingInputChangel() {
        return new PublishSubscribeChannel(executor);
    }
    
    @Bean(MAILCHIMP_TEMPLATE_PROCESSING_FLOW_ID)
    public IntegrationFlow mailchimpTemplateProcessingFlowDefinition() {
        return IntegrationFlows
                .from(MAILCHIMP_TEMPLATE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .handle(endpointPersister)
                .handle(messagePersister)
                .handle(templatedMailchimpMessageSender)
                .handle(logConsumer)
                .get();
    }
    
}