package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.MediaType;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EmailMessageAggregationStrategy extends BasePayloadAggregationStrategy {
	
	public static final String SUBJECT_CONCAT_DELIMITER = "/";
	public static final String PLAIN_TEXT_CONCAT_DELIMITER = "\n\n";
	public static final String HTML_TEXT_CONCAT_DELIMITER = "<br>";
	
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
	protected Optional<PayloadValidationException> checkContentTypes(List<? extends BasePayload> payloads, Class<?> clazz) {
		Optional<PayloadValidationException> exception = super.checkContentTypes(payloads, clazz);
		if (exception.isPresent()) {
			return exception;
		}
		
		Optional<? extends BasePayload> firstPayload = payloads.stream().findFirst();
		if (!firstPayload.isPresent()) {
			return Optional.empty();
		}
		
		String firstPayloadContentType = firstPayload.get().<EmailContent>getContentTyped().getContentType();
		if (!MediaType.TEXT_HTML_VALUE.equals(firstPayloadContentType) && !MediaType.TEXT_PLAIN_VALUE.equals(firstPayloadContentType)) {
			return firstPayload.map(payload -> new PayloadValidationException(
					String.format("Payload %s has content of invalid type. Is %s", payload, payload.<EmailContent>getContentTyped().getContentType())));
		}
		
		Optional<? extends BasePayload> invalidPayload = payloads.stream()
				.filter(payload -> !firstPayload.get().<EmailContent>getContentTyped().getContentType()
						.equals(payload.<EmailContent>getContentTyped().getContentType()))
				.findFirst();
		
		return invalidPayload.map(payload -> new PayloadValidationException(
				String.format("Payload %s has content of invalid type. Is %s", payload, payload.<EmailContent>getContentTyped().getContentType())));
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
		if (payloads.isEmpty()) return null;
		
		EmailContent aggregatedEmailContent = payloads
				.stream()
				.map(BasePayload::<EmailContent>getContentTyped)
				.reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		
		
		Message outputMessage = (Message) payloads.get(0);
		outputMessage.getBody().setMessage(aggregatedEmailContent);
		return outputMessage;
	}
	
	private EmailContent concatContents(EmailContent one, EmailContent other) {
		EmailContent concated = new EmailContent();
		concated.setSubject(one.getSubject().concat(SUBJECT_CONCAT_DELIMITER).concat(other.getSubject()));
		
		concated.setContentType(one.getContentType());
		if (MediaType.TEXT_HTML_VALUE.equals(concated.getContentType())) {
			Document oneAsDocument = Jsoup.parse(one.getText());
			oneAsDocument.body().append(HTML_TEXT_CONCAT_DELIMITER);
			
			Document otherAsDocument = Jsoup.parse(other.getText());
			otherAsDocument.body().childNodesCopy().forEach(oneAsDocument.body()::appendChild);
			
			String concatedTexts = oneAsDocument.html();
			concated.setText(concatedTexts);
		} else if (MediaType.TEXT_PLAIN_VALUE.equals(concated.getContentType())) {
			String concatedTexts = one.getText().concat(PLAIN_TEXT_CONCAT_DELIMITER).concat(other.getText());
			concated.setText(concatedTexts);
		}
		
		concated.getAttachments().addAll(one.getAttachments());
		concated.getAttachments().addAll(other.getAttachments());
		return concated;
	}

}