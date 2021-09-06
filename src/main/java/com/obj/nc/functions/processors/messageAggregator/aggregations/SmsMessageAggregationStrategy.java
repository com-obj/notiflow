package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.List;
import java.util.Optional;

import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmsMessage;
import com.obj.nc.exceptions.PayloadValidationException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SmsMessageAggregationStrategy extends BasePayloadAggregationStrategy<SimpleTextContent> {
	
	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<Message<SimpleTextContent>> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, SimpleTextContent.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return checkReceivingEndpoints(payloads);
	}
	
	@Override
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<Message<SimpleTextContent>> payloads) {
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, SmsEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<Message<SimpleTextContent>> payloads) {
		if (payloads.isEmpty()) return null;
		

		SimpleTextContent aggregatedSmsContent = payloads
				.stream()
				.map(Message::getBody)
				.reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		
		SmsMessage outputMessage = new SmsMessage();
		outputMessage.setBody(aggregatedSmsContent);
		return outputMessage;
	}
	
	private SimpleTextContent concatContents(SimpleTextContent a, SimpleTextContent b) {
		SimpleTextContent concated = new SimpleTextContent();
		concated.setText(a.getText().concat(TEXT_CONCAT_DELIMITER).concat(b.getText()));
		return concated;
	}

}