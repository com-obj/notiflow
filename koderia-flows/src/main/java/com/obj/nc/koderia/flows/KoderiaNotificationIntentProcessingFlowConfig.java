package com.obj.nc.koderia.flows;

import com.obj.nc.functions.processors.eventIdGenerator.GenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinder;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import static com.obj.nc.koderia.flows.MaichimpProcessingFlowConfig.MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID;

@Configuration
@AllArgsConstructor
public class KoderiaNotificationIntentProcessingFlowConfig {
    
    public final static String KODERIA_INTENT_PROCESSING_FLOW_ID = "KODERIA_INTENT_PROCESSING_FLOW_ID";
    public final static String KODERIA_INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID = KODERIA_INTENT_PROCESSING_FLOW_ID + "_INPUT";
    
    private final GenerateEventIdProcessingFunction generateEventId;
    private final KoderiaRecipientsFinder recipientsFinder;
    private final MessagesFromNotificationIntentProcessingFunction generateMessagesFromEvent;
    
    @Bean(KODERIA_INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel koderiaIntentProcessingInputChangel() {
        return new PublishSubscribeChannel();
    }
    
    @Bean(KODERIA_INTENT_PROCESSING_FLOW_ID)
    public IntegrationFlow koderiaIntentProcessingFlowDefinition() {
        return IntegrationFlows
                .from(KODERIA_INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .transform(generateEventId)
                .transform(recipientsFinder)
                .transform(generateMessagesFromEvent)
                .split()
                .channel(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .get();
    }
    
}
