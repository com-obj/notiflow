/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.functions.processors.senders.mailchimp;

import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.mailchimp.BaseMailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfigProperties;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpAttachmentDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpRecipientDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpSendRequestDto;
import com.obj.nc.functions.processors.senders.mailchimp.dtos.MailchimpSendResponseDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfig.MAILCHIMP_REST_TEMPLATE;

@Getter
public abstract class BaseMailchimpSender<T extends BaseMailchimpContent> extends ProcessorFunctionAdapter<Message<T>, Message<T>> {
	
	@Qualifier(MAILCHIMP_REST_TEMPLATE)
	@Autowired private RestTemplate restTemplate;
	@Autowired private MailchimpSenderConfigProperties properties;
	
	private final String sendApiPath;
	
	public BaseMailchimpSender(String sendApiPath) {
		this.sendApiPath = sendApiPath;
	}

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message<T> payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("Message must not be null"));
		}
		
		MessageContent content = payload.getBody();
		if (content == null) {
			return Optional.of(new PayloadValidationException("Message content must not be null"));
		}
		
		boolean hasNoneOrTooMuchEndpoints = payload.getReceivingEndpoints().size() != 1;
		boolean containsNonEmailEndpoint = payload.getReceivingEndpoints().stream()
				.anyMatch(endpoint -> !MailchimpEndpoint.JSON_TYPE_IDENTIFIER.equals(endpoint.getEndpointType()));
		
		if (hasNoneOrTooMuchEndpoints || containsNonEmailEndpoint) {
			return Optional.of(new PayloadValidationException(String.format("MailchimpSender can only send message %s to 1 MailchimpEndpoint", payload)));
		}
		
		return Optional.empty();
	}

	@Override
	protected Message<T> execute(Message<T> payload) {
		MailchimpSendRequestDto sendRequestDto = createSendRequestBody(payload);
		
		ResponseEntity<MailchimpSendResponseDto[]> responseEntity = restTemplate
				.postForEntity(sendApiPath, sendRequestDto, MailchimpSendResponseDto[].class);
		
		if (responseEntity.getBody() == null) {
			throw new RestClientException("Response body must not be null");
		}
		
		Optional<MailchimpSendResponseDto> errorResponse = Arrays.stream(responseEntity.getBody())
				.filter(response -> response.getRejectReason() != null)
				.findFirst();
		
		if (errorResponse.isPresent()) {
			throw new RuntimeException(String.format("Failed to send Mailchimp message. Reject reason: %s", errorResponse.get().getRejectReason()));
		}
		
		return payload;
	}
	
	public abstract MailchimpSendRequestDto createSendRequestBody(Message<T> payload);
	
	public List<MailchimpRecipientDto> mapRecipientsToDto(List<? extends ReceivingEndpoint> receivingEndpoints) {
		return receivingEndpoints.stream()
				.map(receivingEndpoint ->
						MailchimpRecipientDto.builder()
								.email(receivingEndpoint.getEndpointId())
								.build())
				.collect(Collectors.toList());
	}
	
	public List<MailchimpAttachmentDto> mapAttachmentsToDto(List<Attachment> attachments) {
		return attachments.stream()
				.map(MailchimpAttachmentDto::fromAttachment)
				.collect(Collectors.toList());
	}

}
