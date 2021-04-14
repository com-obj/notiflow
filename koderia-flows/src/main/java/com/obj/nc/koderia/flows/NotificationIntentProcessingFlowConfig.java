package com.obj.nc.koderia.flows;

import com.obj.nc.functions.processors.eventIdGenerator.ValidateAndGenerateEventIdProcessingFunction;
import com.obj.nc.functions.processors.messageBuilder.MessagesFromNotificationIntentProcessingFunction;
import com.obj.nc.koderia.functions.processors.messageExtractor.KoderiaMessageExtractorProcessorFunction;
import com.obj.nc.koderia.functions.processors.recipientsFinder.KoderiaRecipientsFinderProcessorFunction;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import static com.obj.nc.flows.config.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_ID;
import static com.obj.nc.flows.config.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.koderia.flows.MaichimpProcessingFlowConfig.MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID;

@Configuration
@AllArgsConstructor
public class NotificationIntentProcessingFlowConfig {
    
    private final ValidateAndGenerateEventIdProcessingFunction generateEventId;
    private final MessagesFromNotificationIntentProcessingFunction generateMessagesFromEvent;
    private final KoderiaMessageExtractorProcessorFunction messageExtractor;
    private final KoderiaRecipientsFinderProcessorFunction recipientsFinder;
    
    @Bean(INTENT_PROCESSING_FLOW_ID)
    public IntegrationFlow intentProcessingFlowDefinition() {
        return IntegrationFlows
                .from(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .transform(generateEventId)
                .transform(messageExtractor)
                .transform(recipientsFinder)
                .transform(generateMessagesFromEvent)
                .split()
                .channel(MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .get();
    }
    
}
