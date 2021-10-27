package com.obj.nc.flows.slackMessageProcessingFlow;

import com.obj.nc.domain.message.SlackMessage;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import java.util.concurrent.Future;

import static com.obj.nc.flows.slackMessageProcessingFlow.SlackMessageProcessingFlowConfig.SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.slackMessageProcessingFlow.SlackMessageProcessingFlowConfig.SLACK_PROCESSING_FLOW_OUTPUT_CHANNEL_ID;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface SlackMessageProcessingFlow {
    @Gateway(
            requestChannel = SLACK_PROCESSING_FLOW_INPUT_CHANNEL_ID,
            replyChannel = SLACK_PROCESSING_FLOW_OUTPUT_CHANNEL_ID
    )
    Future<SlackMessage> sendMessage(SlackMessage msg);
}
