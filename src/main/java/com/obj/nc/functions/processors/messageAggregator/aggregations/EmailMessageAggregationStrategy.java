package com.obj.nc.functions.processors.messageAggregator.aggregations;

import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;


@Component
public class EmailMessageAggregationStrategy extends BasePayloadAggregationStrategy<EmailContent> {
	
	public static final String SUBJECT_CONCAT_DELIMITER = " / ";
	public static final String PLAIN_TEXT_CONCAT_DELIMITER = "\n\n";
	public static final String HTML_TEXT_CONCAT_DELIMITER = "<br>";
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<Message<EmailContent>> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, EmailContent.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return checkReceivingEndpoints(payloads);
	}
	
	@Override
	protected Optional<PayloadValidationException> checkContentTypes(List<Message<EmailContent>> payloads, Class<EmailContent> clazz) {
		Optional<PayloadValidationException> exception = super.checkContentTypes(payloads, clazz);
		if (exception.isPresent()) {
			return exception;
		}
		
		Optional<Message<EmailContent>> firstPayload = payloads.stream().findFirst();
		if (!firstPayload.isPresent()) {
			return Optional.empty();
		}
		
		String firstPayloadContentType = firstPayload.get().getBody().getContentType();
		if (!MediaType.TEXT_HTML_VALUE.equals(firstPayloadContentType) && !MediaType.TEXT_PLAIN_VALUE.equals(firstPayloadContentType)) {
			return firstPayload.map(payload -> new PayloadValidationException(
					String.format("Payload %s has content of invalid type. Is %s", payload, payload.getBody().getContentType())));
		}
		
		return Optional.empty();
	}
	
	@Override
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<Message<EmailContent>> payloads) {
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, EmailEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<Message<EmailContent>> payloads) {
		if (payloads.isEmpty()) return null;
		
		EmailContent aggregatedEmailContent = payloads
				.stream()
				.map(Message::getBody)
				.reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		
		Message<EmailContent> outputMessage = Message.newTypedMessageFrom(EmailMessage.class, payloads.toArray(new Message[0]));
		outputMessage.setReceivingEndpoints(payloads.get(0).getReceivingEndpoints());
		outputMessage.setBody(aggregatedEmailContent);
		return outputMessage;
	}
	
	private EmailContent concatContents(EmailContent one, EmailContent other) {
		EmailContent concatenated = new EmailContent();
		concatenated.setSubject(one.getSubject().concat(SUBJECT_CONCAT_DELIMITER).concat(other.getSubject()));
		
		concatenated.setContentType(one.getContentType());
		if (MediaType.TEXT_HTML_VALUE.equals(concatenated.getContentType())) {
			Document oneAsDocument = Jsoup.parse(one.getText());
			oneAsDocument.body().append(HTML_TEXT_CONCAT_DELIMITER);
			
			Document otherAsDocument = Jsoup.parse(other.getText());
			otherAsDocument.body().childNodesCopy().forEach(oneAsDocument.body()::appendChild);
			
			String concatenatedTexts = oneAsDocument.html();
			concatenated.setText(concatenatedTexts);
		} else if (MediaType.TEXT_PLAIN_VALUE.equals(concatenated.getContentType())) {
			String concatenatedTexts = one.getText().concat(PLAIN_TEXT_CONCAT_DELIMITER).concat(other.getText());
			concatenated.setText(concatenatedTexts);
		}
		
		concatenated.getAttachments().addAll(one.getAttachments());
		concatenated.getAttachments().addAll(other.getAttachments());
		return concatenated;
	}

}