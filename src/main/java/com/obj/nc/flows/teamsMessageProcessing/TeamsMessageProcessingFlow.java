package com.obj.nc.flows.teamsMessageProcessing;

import com.obj.nc.domain.message.TeamsMessage;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import java.util.concurrent.Future;

import static com.obj.nc.flows.teamsMessageProcessing.TeamsMessageProcessingFlowConfig.TEAMS_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.teamsMessageProcessing.TeamsMessageProcessingFlowConfig.TEAMS_PROCESSING_FLOW_OUTPUT_CHANNEL_ID;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface TeamsMessageProcessingFlow {
    @Gateway(
            requestChannel = TEAMS_PROCESSING_FLOW_INPUT_CHANNEL_ID,
            replyChannel = TEAMS_PROCESSING_FLOW_OUTPUT_CHANNEL_ID
    )
    Future<TeamsMessage> sendMessage(TeamsMessage msg);
}
