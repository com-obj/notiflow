package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;

import com.obj.nc.exceptions.PayloadValidationException;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Optional;

@Log4j2
public class EmailMessageAggregationStrategy extends BasePayloadAggregationStrategy {
	
	public static final String SUBJECT_CONCAT_DELIMITER = "/";
	public static final String TEXT_CONCAT_DELIMITER = "\n\n";
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<? extends BasePayload> payloads) {
		Optional<PayloadValidationException> exception = checkContentClassTypes(payloads, EmailContent.class);
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
	protected Optional<PayloadValidationException> checkDeliveryOptions(List<? extends BasePayload> payloads) {
		Optional<PayloadValidationException> exception = super.checkDeliveryOptions(payloads);
		if (exception.isPresent()) {
			return exception;
		}
		
		Optional<? extends BasePayload> firstPayload = payloads.stream().findFirst();
		if (!firstPayload.isPresent()) {
			return Optional.empty();
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
		
		Message outputMessage = Message.createAsEmail();
		outputMessage.getBody().setRecievingEndpoints(payloads.get(0).getBody().getRecievingEndpoints());
		outputMessage.getBody().setDeliveryOptions(payloads.get(0).getBody().getDeliveryOptions());

		EmailContent aggregatedEmailContent = payloads.stream().map(BasePayload::<EmailContent>getContentTyped).reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		outputMessage.getBody().setMessage(aggregatedEmailContent);
		return outputMessage;
	}
	
	private EmailContent concatContents(EmailContent one, EmailContent other) {
		EmailContent concated = new EmailContent();
		concated.setContentType(one.getContentType());
		concated.setSubject(one.getSubject().concat(SUBJECT_CONCAT_DELIMITER).concat(other.getSubject()));
		
		String concatedTexts = one.getTextForAggregation().concat(TEXT_CONCAT_DELIMITER).concat(other.getTextForAggregation());
		if (MediaType.TEXT_HTML_VALUE.equals(concated.getContentType())) {
			Document concatedTextsAsDocument = Jsoup.parseBodyFragment(concatedTexts);
			Document oneAsDocument = Jsoup.parse(one.getText());
			oneAsDocument.body().replaceWith(concatedTextsAsDocument.body());
			concatedTexts = oneAsDocument.html();
		}
		
		concated.setText(concatedTexts);
		concated.getAttachments().addAll(one.getAttachments());
		concated.getAttachments().addAll(other.getAttachments());
		return concated;
	}

}