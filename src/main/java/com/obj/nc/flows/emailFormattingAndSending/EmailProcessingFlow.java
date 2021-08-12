package com.obj.nc.flows.emailFormattingAndSending;

import java.util.concurrent.Future;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.*;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface EmailProcessingFlow {

	@Gateway(requestChannel=EMAIL_SEND_FLOW_INPUT_CHANNEL_ID, replyChannel = EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID)
    Future<EmailMessage> sendEmail(EmailMessage msg);
	
	@Gateway(requestChannel= EMAIL_FORMAT_AND_SEND_FLOW_INPUT_CHANNEL_ID, replyChannel = EMAIL_SEND_FLOW_OUTPUT_CHANNEL_ID)
    Future<EmailMessage> formatAndSend(EmailMessageTemplated<?> msg);
}
