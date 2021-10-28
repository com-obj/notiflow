package com.obj.nc.flows.slackMessageProcessingFlow;

import com.obj.nc.functions.processors.endpointPersister.EndpointPersister;
import com.obj.nc.functions.processors.messagePersister.MessagePersister;
import com.obj.nc.functions.processors.senders.slack.SlackMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static com.obj.nc.flows.deliveryInfo.DeliveryInfoFlowConfig.DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID;

@Configuration
@RequiredArgsConstructor
public class SlackMessageProcessingFlowConfig {
    public final static String SLACK_PROCESSING_FLOW_ID = "SLACK_PROCESSING_FLOW_ID";
    public final static String SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID = SLACK_PROCESSING_FLOW_ID + "_INPUT";
    public final static String SLACK_PROCESSING_FLOW_OUTPUT_CHANNEL_ID = SLACK_PROCESSING_FLOW_ID + "_OUTPUT";

    private final SlackMessageSender slackMessageSender;
    private final MessagePersister messagePersister;
    private final EndpointPersister endpointPersister;
    private final ThreadPoolTaskScheduler executor;

    @Bean(SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID)
    public MessageChannel pushSendInputChannel() {
        return new PublishSubscribeChannel(executor);
    }

    @Bean(SLACK_PROCESSING_FLOW_OUTPUT_CHANNEL_ID)
    public MessageChannel pushSendOutputChannel() {
        return new PublishSubscribeChannel(executor);
    }

    @Bean(SLACK_PROCESSING_FLOW_ID)
    public IntegrationFlow pushSendFlow() {
        return IntegrationFlows
                .from(pushSendInputChannel())
                .handle(endpointPersister)
                .handle(messagePersister)
                .handle(slackMessageSender)
                .wireTap(flowConfig -> flowConfig.channel(DELIVERY_INFO_SEND_FLOW_INPUT_CHANNEL_ID))
                .channel(pushSendOutputChannel())
                .get();
    }
}
