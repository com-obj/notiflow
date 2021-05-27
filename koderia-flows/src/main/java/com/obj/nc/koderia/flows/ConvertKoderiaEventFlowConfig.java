package com.obj.nc.koderia.flows;

import com.obj.nc.functions.processors.eventFactory.MailchimpEventConverter;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinder;
import lombok.AllArgsConstructor;

import static com.obj.nc.flows.messageProcessing.MessageProcessingFlowConfig.MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

@Configuration
@AllArgsConstructor
public class ConvertKoderiaEventFlowConfig {
    
    public final static String CONVERT_KODERIA_EVENT_FLOW_ID = "default-flow";
    public final static String CONVERT_KODERIA_EVENT_FLOW_INPUT_CHANNEL_ID = CONVERT_KODERIA_EVENT_FLOW_ID + "_INPUT";
    
    private final MailchimpEventConverter mailchimpEventConverter;
    private final KoderiaRecipientsFinder recipientsFinder;
    
    @Bean(CONVERT_KODERIA_EVENT_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel convertKoderiaEventFlowInputChangel() {
        return new PublishSubscribeChannel();
    }
    
    @Bean(CONVERT_KODERIA_EVENT_FLOW_ID)
    public IntegrationFlow convertKoderiaEventFlowDefinition() {
        return IntegrationFlows
                .from(CONVERT_KODERIA_EVENT_FLOW_INPUT_CHANNEL_ID)
                .handle(mailchimpEventConverter)
                .handle(recipientsFinder)
                .channel(MESSAGE_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .get();
    }
    
}
