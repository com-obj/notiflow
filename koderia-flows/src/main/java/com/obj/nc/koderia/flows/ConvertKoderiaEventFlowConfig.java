package com.obj.nc.koderia.flows;

import com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;

import static com.obj.nc.flows.config.NotificationIntentProcessingFlowConfig.INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID;

@Configuration
@AllArgsConstructor
public class ConvertKoderiaEventFlowConfig {
    
    public final static String CONVERT_KODERIA_EVENT_FLOW_ID = "default-flow";
    public final static String CONVERT_KODERIA_EVENT_FLOW_INPUT_CHANNEL_ID = CONVERT_KODERIA_EVENT_FLOW_ID + "_INPUT";
    
    private final GenericEventToNotificaitonIntentConverter eventConverter;
    
    @Bean(CONVERT_KODERIA_EVENT_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel convertKoderiaEventFlowInputChangel() {
        return new PublishSubscribeChannel();
    }
    
    @Bean(CONVERT_KODERIA_EVENT_FLOW_ID)
    public IntegrationFlow convertKoderiaEventFlowDefinition() {
        return IntegrationFlows
                .from(CONVERT_KODERIA_EVENT_FLOW_INPUT_CHANNEL_ID)
                .transform(eventConverter)
                .split()
                .channel(INTENT_PROCESSING_FLOW_INPUT_CHANNEL_ID)
                .get();
    }
    
}
