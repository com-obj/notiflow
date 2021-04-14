package com.obj.nc.koderia.functions.processors.mailchimpSender;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.AggregatedEmailContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import com.obj.nc.koderia.dto.mailchimp.MessageResponseDto;
import com.obj.nc.koderia.dto.mailchimp.SendMessageWithTemplateDto;
import com.obj.nc.koderia.mapper.MailchimpMessageMapper;
import com.obj.nc.koderia.mapper.MailchimpMessageMapperAggregateImpl;
import com.obj.nc.koderia.mapper.MailchimpMessageMapperImpl;
import com.obj.nc.koderia.services.MailchimpClient;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.obj.nc.functions.processors.eventFactory.GenericEventToNotificaitonIntentConverter.ORIGINAL_EVENT_FIELD;
import static com.obj.nc.koderia.functions.processors.mailchimpSender.MailchimpSenderConfig.*;

@Component
@DocumentProcessingInfo("SendMailchimpMessage")
public class MailchimpSenderProcessorFunction extends ProcessorFunctionAdapter<Message, Message> implements MailchimpClient {
	@Qualifier(MAILCHIMP_REST_TEMPLATE)
	@Autowired private RestTemplate restTemplate;
	@Qualifier(MailchimpMessageMapperImpl.COMPONENT_NAME)
	@Autowired private MailchimpMessageMapper mailchimpMessageMapper;
	@Qualifier(MailchimpMessageMapperAggregateImpl.COMPONENT_NAME)
	@Autowired private MailchimpMessageMapper mailchimpAggregateMessageMapper;

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Message must not be null"));
		}

		Content messageContent = payload.getBody().getMessage();
		if (messageContent instanceof AggregatedEmailContent) {
			AggregatedEmailContent aggregatedContent = (AggregatedEmailContent)messageContent;
			
			for (EmailContent messageContentPart : aggregatedContent.getAggregateContent()) {
				
				Optional<PayloadValidationException> exception = checkMessageContent(messageContentPart);
				if (exception.isPresent()) {
					return exception;
				}
			}
		} else {
			Optional<PayloadValidationException> exception = checkMessageContent(payload.getContentTyped());
			if (exception.isPresent()) {
				return exception;
			}
		}

		return checkReceivingEndpoints(payload);
	}

	@Override
	protected Message execute(Message payload) {
		SendMessageWithTemplateDto sendMessageDto = payload.isAggregateMessage()
				? mailchimpAggregateMessageMapper.mapWithTemplate(payload)
				: mailchimpMessageMapper.mapWithTemplate(payload);
		
		List<MessageResponseDto> messageResponseDtos = sendMessageWithTemplate(sendMessageDto);
		payload.getBody().setAttributeValue(MAILCHIMP_RESPONSE_FIELD, messageResponseDtos);
		return payload;
	}
	
	@Override
	public List<MessageResponseDto> sendMessageWithTemplate(SendMessageWithTemplateDto sendMessageDto) {
		ResponseEntity<MessageResponseDto[]> responseEntity = restTemplate.postForEntity(SEND_TEMPLATE_PATH, sendMessageDto, MessageResponseDto[].class);
		MessageResponseDto[] responseBody = responseEntity.getBody();
		
		if (responseBody == null) {
			throw new RestClientException("Response body is null");
		}
		
		return Arrays.stream(responseBody).collect(Collectors.toList());
	}
	
	@Override
	public RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	private Optional<PayloadValidationException> checkMessageContent(EmailContent messageContent) {
		if (!messageContent.containsAttributes(Collections.singletonList(ORIGINAL_EVENT_FIELD))) {
			return Optional.of(new PayloadValidationException(String.format("Message must contain attribute: %s", ORIGINAL_EVENT_FIELD)));
		}

		if (!messageContent.containsNestedAttributes(Collections.singletonList("type"), ORIGINAL_EVENT_FIELD)) {
			return Optional.of(new PayloadValidationException(String.format("Message must contain attribute: %s.type", ORIGINAL_EVENT_FIELD)));
		}

		if (!StringUtils.hasText(messageContent.getSubject())) {
			return Optional.of(new PayloadValidationException("Message must contain Subject with at least 1 non-whitespace character"));
		}

		return Optional.empty();
	}

	private Optional<PayloadValidationException> checkReceivingEndpoints(Message message) {
		boolean hasNoneOrTooMuchEndpoints = message.getBody().getRecievingEndpoints().size() != 1;
		boolean containsNonEmailEndpoint = message.getBody().getRecievingEndpoints().stream()
				.anyMatch(endpoint -> !EmailEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointTypeName()));

		if (hasNoneOrTooMuchEndpoints || containsNonEmailEndpoint) {
			return Optional.of(new PayloadValidationException(String.format("Mailchimp can only send message %s to 1 EmailContent endpoint", message)));
		}

		return Optional.empty();
	}
}
