package com.obj.nc.flows.emailFormattingAndSending;

import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_FROMAT_AND_SEND_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.emailFormattingAndSending.EmailProcessingFlowConfig.EMAIL_SENDING_FLOW_OUTPUT_CHANNEL_ID;

import java.util.concurrent.Future;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.EmailMessageTemplated;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface EmailProcessingFlow {

	@Gateway(requestChannel=EMAIL_SENDING_FLOW_INPUT_CHANNEL_ID, replyChannel = EMAIL_SENDING_FLOW_OUTPUT_CHANNEL_ID)
    Future<EmailMessage> sendEmail(EmailMessage msg);
	
	@Gateway(requestChannel=EMAIL_FROMAT_AND_SEND_INPUT_CHANNEL_ID, replyChannel = EMAIL_SENDING_FLOW_OUTPUT_CHANNEL_ID)
    Future<EmailMessage> formatAndSend(EmailMessageTemplated<?> msg);
}
