package com.obj.nc.flows.testmode.email.functions.processors;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import com.obj.nc.domain.content.AggregatedContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.headers.Header;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.flows.testmode.TestModeProperties;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AggregatedContentToSingleEmailTransformer extends ProcessorFunctionAdapter<Message, Message> {
	
	private TestModeProperties testModeProps;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
		if (!(payload.getBody().getMessage() instanceof AggregatedContent)) {
			throw new PayloadValidationException("Cannot process message with content other then AggregatedContent. Is " + payload.getBody().getMessage());
		}
		
		//TODO: check if payload.getBody().getMessage() contains only EmailContent or SimpleTextContent
		
		return Optional.empty();
	}

	@Override
	//TODO: ked bude refactorovany header a ostatne veci tak tuto spravit novu message a neprepisovat existujucu
	protected Message execute(Message aggregatedMessage) {
		TestModeDiggestModel digestModel = new TestModeDiggestModel();
		
		AggregatedContent aggregate = ((AggregatedContent)aggregatedMessage.getBody().getMessage());
		aggregate.getAggregateContent().stream().forEach(e -> {
			if (e instanceof EmailContent) {
				digestModel.addEmailContent((EmailContent)e);
			} else if (e instanceof SimpleTextContent) {
				digestModel.addSmsContent((SimpleTextContent)e);
			}
		});
		
		TestModeDiggestMailContent resultMail = new TestModeDiggestMailContent();
		resultMail.setSubject("Notifications digest while running test mode");
		resultMail.setTemplateFileName("test-mode-digest.html");
		resultMail.setRequiredLocales(Arrays.asList(new Locale("en")));
		resultMail.setModel(digestModel);

		aggregatedMessage.getBody().setMessage(resultMail);
		
		List<RecievingEndpoint> emailEndpoints = testModeProps.getRecipients().stream().map(rec-> new EmailEndpoint(rec)).collect(Collectors.toList());
		aggregatedMessage.getBody().setRecievingEndpoints(emailEndpoints);
		
		aggregatedMessage.getHeader().setAttributeValue(Header.SUPRESS_GENERATE_PROC_INFO_PARAM_NAME, true);

		return aggregatedMessage;
	}

}
