package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;

import com.obj.nc.exceptions.PayloadValidationException;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

@Log4j2
public class SmsMessageAggregationStrategy extends BasePayloadAggregationStrategy {
	
	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<? extends BasePayload> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, SimpleTextContent.class);
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
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, SmsEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<? extends BasePayload> payloads) {
		Message outputMessage = Message.createAsSms();
		outputMessage.getBody().setRecievingEndpoints(payloads.get(0).getBody().getRecievingEndpoints());
		outputMessage.getBody().setDeliveryOptions(payloads.get(0).getBody().getDeliveryOptions());
		
		SimpleTextContent aggregatedSmsContent = payloads.stream().map(BasePayload::<SimpleTextContent>getContentTyped).reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		outputMessage.getBody().setMessage(aggregatedSmsContent);
		return outputMessage;
	}
	
	private SimpleTextContent concatContents(SimpleTextContent a, SimpleTextContent b) {
		SimpleTextContent concated = new SimpleTextContent();
		concated.setText(a.getText().concat(TEXT_CONCAT_DELIMITER).concat(b.getText()));
		return concated;
	}

}