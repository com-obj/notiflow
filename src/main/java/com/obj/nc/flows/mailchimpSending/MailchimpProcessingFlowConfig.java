package com.obj.nc.flows.mailchimpSending;

import com.obj.nc.functions.processors.messageTemplating.config.TrackingConfigProperties;
import com.obj.nc.functions.processors.messageTracking.MailchimpReadTrackingDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.senders.mailchimp.MailchimpMessageSender;

import lombok.RequiredArgsConstructor;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

@Configuration
@RequiredArgsConstructor
public class MailchimpProcessingFlowConfig {
    
    public final static String MAILCHIMP_PROCESSING_FLOW_ID = "MAILCHIMP_PROCESSING_FLOW_ID";
    public final static String MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID = MAILCHIMP_PROCESSING_FLOW_ID + "_INPUT";
    public static final String MAILCHIMP_PROCESSING_FLOW_OUTPUT_CHANNEL_ID = MAILCHIMP_PROCESSING_FLOW_ID + "_OUTPUT";
    
    private final MailchimpMessageSender mailchimpMessageSender;
    private final MailchimpReadTrackingDecorator readTrackingDecorator;
    private final TrackingConfigProperties trackingConfigProperties;
    private final MessagePersister messagePersister;
    private final EndpointPersister endpointPersister;
    private final ThreadPoolTaskScheduler executor;
    
    @Bean(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel mailchimpProcessingInputChangel() {
        return new PublishSubscribeChannel(executor);
    }
    
    @Bean(MAILCHIMP_PROCESSING_FLOW_ID)
    public IntegrationFlow mailchimpProcessingFlowDefinition() {
        return IntegrationFlows
                .from(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .handle(endpointPersister)
                .handle(messagePersister)
                .routeToRecipients(spec -> spec
                        .recipientFlow(source -> trackingConfigProperties.isEnabled(),
                                trackingSubflow -> trackingSubflow
                                        .handle(readTrackingDecorator)
                                        .handle(messagePersister)
                                        .channel(internalEmailSendFlowDefinition().getInputChannel()))
                        .defaultOutputChannel(internalEmailSendFlowDefinition().getInputChannel()))
                .get();
    }
    
    @Bean("INTERNAL_MAILCHIMP_SEND_FLOW_ID")
    public IntegrationFlow internalEmailSendFlowDefinition() {
        return flow -> flow
                .handle(mailchimpMessageSender)
                .wireTap(flowConfig -> flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID))
                .channel(mailchimpSendOutputChannel()); 
    }
    
    @Bean(MAILCHIMP_PROCESSING_FLOW_OUTPUT_CHANNEL_ID)
    public MessageChannel mailchimpSendOutputChannel() {
        return new PublishSubscribeChannel(executor);
    }
    
}
