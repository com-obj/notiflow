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

package com.obj.nc.functions.processors.messageAggregator.aggregations;

import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.content.mailchimp.MailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.message.MailchimpMessage;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class MailchimpMessageAggregationStrategy extends BasePayloadAggregationStrategy<MailchimpContent> {
	
	private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<Message<MailchimpContent>> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, MailchimpContent.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return checkReceivingEndpoints(payloads);
	}
	
	@Override
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<Message<MailchimpContent>> payloads) {
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, MailchimpEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<Message<MailchimpContent>> payloads) {
		if (payloads.isEmpty()) return null;
		
		MailchimpContent aggregatedMailchimpContent = payloads
				.stream()
				.map(Message::getBody)
				.reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		
		MailchimpMessage outputMessage = new MailchimpMessage();
		outputMessage.setBody(aggregatedMailchimpContent);
		outputMessage.setReceivingEndpoints(payloads.get(0).getReceivingEndpoints());
		return outputMessage;
	}
	
	private MailchimpContent concatContents(MailchimpContent one, MailchimpContent other) {
		MailchimpContent aggregatedContent = new MailchimpContent();
		
		aggregatedContent.setSubject(mailchimpSenderConfigProperties.getAggregatedMessageSubject());
		
		ArrayList<Attachment> dtos = new ArrayList<>(one.getAttachments());
		dtos.addAll(other.getAttachments());
		
		aggregatedContent.setAttachments(dtos);
		
		return aggregatedContent;
	}

}