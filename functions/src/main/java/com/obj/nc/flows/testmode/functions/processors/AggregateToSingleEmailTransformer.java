package com.obj.nc.flows.testmode.functions.processors;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

public class AggregateToSingleEmailTransformer extends ProcessorFunctionAdapter<Message, Message> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
		if (!(payload.getBody().getMessage() instanceof AggregatedEmailContent)) {
			throw new PayloadValidationException("Cannot process message with content other then AggregatedEmailContent. Is " + payload.getBody().getMessage());
		}
		return Optional.empty();
	}

	@Override
	protected Message execute(Message aggregatedMessage) {
		TestModeDiggestModel digestModel = new TestModeDiggestModel();
		
		AggregatedEmailContent aggregate = ((AggregatedEmailContent)aggregatedMessage.getBody().getMessage());
		aggregate.getAggregateContent().stream().forEach(e -> {
			digestModel.getEmailContents().add(e);
			if (!e.getContentType().equals(MediaType.TEXT_HTML_VALUE)) {
				return;
			}
			
			//TODO: replace <Body> with lowercase
			String bodyPartHtml = StringUtils.substringBetween(e.getText(), "<body>", "</body>");
			e.setText("<div>" + bodyPartHtml + "</div>");
		});
		
		TestModeDiggestMailContent resultMail = new TestModeDiggestMailContent();
		resultMail.setSubject("Notifications digest while running test mode");
		resultMail.setTemplateFileName("test-mode-digest.html");
		resultMail.setRequiredLocales(Arrays.asList(new Locale("en")));
		resultMail.setModel(digestModel);

		aggregatedMessage.getBody().setMessage(resultMail);

		return aggregatedMessage;
	}

}
