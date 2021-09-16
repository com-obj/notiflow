package com.obj.nc.flows.mailchimpSending;

import com.obj.nc.domain.message.MailchimpMessage;
import com.obj.nc.flows.errorHandling.ErrorHandlingFlowConfig;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import java.util.concurrent.Future;

import static com.obj.nc.flows.mailchimpSending.MailchimpProcessingFlowConfig.MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID;
import static com.obj.nc.flows.mailchimpSending.MailchimpProcessingFlowConfig.MAILCHIMP_PROCESSING_FLOW_OUTPUT_CHANNEL_ID;

@MessagingGateway(errorChannel = ErrorHandlingFlowConfig.ERROR_CHANNEL_NAME)
public interface MailchimpProcessingFlow {

	@Gateway(requestChannel=MAILCHIMP_PROCESSING_FLOW_INPUT_CHANNEL_ID, replyChannel = MAILCHIMP_PROCESSING_FLOW_OUTPUT_CHANNEL_ID)
    Future<MailchimpMessage> sendMailchimpMessage(MailchimpMessage msg);
	
}
