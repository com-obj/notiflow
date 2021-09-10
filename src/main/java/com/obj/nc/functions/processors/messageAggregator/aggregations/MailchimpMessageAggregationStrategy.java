package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.message.MailchimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class MailchimpMessageAggregationStrategy extends BasePayloadAggregationStrategy<MailchimpContent> {
	
	private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<Message<MailchimpContent>> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, MailchimpContent.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return checkReceivingEndpoints(payloads);
	}
	
	@Override
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<Message<MailchimpContent>> payloads) {
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, MailchimpEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<Message<MailchimpContent>> payloads) {
		if (payloads.isEmpty()) return null;
		
		MailchimpContent aggregatedMailchimpContent = payloads
				.stream()
				.map(Message::getBody)
				.reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		
		MailchimpMessage outputMessage = new MailchimpMessage();
		outputMessage.setBody(aggregatedMailchimpContent);
		outputMessage.setReceivingEndpoints(payloads.get(0).getReceivingEndpoints());
		return outputMessage;
	}
	
	private MailchimpContent concatContents(MailchimpContent one, MailchimpContent other) {
		MailchimpContent aggregatedContent = new MailchimpContent();
		
		aggregatedContent.setSubject(mailchimpSenderConfigProperties.getAggregatedMessageSubject());
		
		ArrayList<Attachment> dtos = new ArrayList<>(one.getAttachments());
		dtos.addAll(other.getAttachments());
		
		aggregatedContent.setAttachments(dtos);
		
		return aggregatedContent;
	}

}