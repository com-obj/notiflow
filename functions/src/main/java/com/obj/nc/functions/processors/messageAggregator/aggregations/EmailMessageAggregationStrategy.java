package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;

import com.obj.nc.exceptions.PayloadValidationException;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

@Log4j2
public class EmailMessageAggregationStrategy extends BasePayloadAggregationStrategy {
	
	public static final String SUBJECT_CONCAT_DELIMITER = "/";
	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<? extends BasePayload> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, EmailContent.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		exception = checkDeliveryOptions(payloads);
		if (exception.isPresent()) {
			return exception;
		}
		
		return checkReceivingEndpoints(payloads);
	}
	
	@Override
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<? extends BasePayload> payloads) {
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, EmailEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<? extends BasePayload> payloads) {
		Message outputMessage = Message.createAsEmail();
		outputMessage.getBody().setRecievingEndpoints(payloads.get(0).getBody().getRecievingEndpoints());
		outputMessage.getBody().setDeliveryOptions(payloads.get(0).getBody().getDeliveryOptions());

		EmailContent aggregatedEmailContent = payloads.stream().map(BasePayload::<EmailContent>getContentTyped).reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		outputMessage.getBody().setMessage(aggregatedEmailContent);
		return outputMessage;
	}
	
	private EmailContent concatContents(EmailContent a, EmailContent b) {
		EmailContent concated = new EmailContent();
		concated.setSubject(a.getSubject().concat(SUBJECT_CONCAT_DELIMITER).concat(b.getSubject()));
		concated.setText(a.getTextForAggregation().concat(TEXT_CONCAT_DELIMITER).concat(b.getTextForAggregation()));
		concated.getAttachments().addAll(a.getAttachments());
		concated.getAttachments().addAll(b.getAttachments());
		return concated;
	}

}