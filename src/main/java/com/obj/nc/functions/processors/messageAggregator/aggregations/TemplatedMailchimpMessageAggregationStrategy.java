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
import com.obj.nc.domain.content.mailchimp.TemplatedMailchimpContent;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.TemplatedMailchimpMessage;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.senders.mailchimp.config.MailchimpSenderConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
public class TemplatedMailchimpMessageAggregationStrategy extends BasePayloadAggregationStrategy<TemplatedMailchimpContent> {
	
	private final MailchimpSenderConfigProperties mailchimpSenderConfigProperties;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(List<Message<TemplatedMailchimpContent>> payloads) {
		Optional<PayloadValidationException> exception = checkContentTypes(payloads, TemplatedMailchimpContent.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return checkReceivingEndpoints(payloads);
	}
	
	@Override
	protected Optional<PayloadValidationException> checkReceivingEndpoints(List<Message<TemplatedMailchimpContent>> payloads) {
		Optional<PayloadValidationException> exception = checkEndpointTypes(payloads, MailchimpEndpoint.class);
		if (exception.isPresent()) {
			return exception;
		}
		
		return super.checkReceivingEndpoints(payloads);
	}
	
	@Override
	public Object merge(List<Message<TemplatedMailchimpContent>> payloads) {
		if (payloads.isEmpty()) return null;
		
		TemplatedMailchimpContent aggregatedMailchimpContent = payloads
				.stream()
				.map(Message::getBody)
				.reduce(this::concatContents)
				.orElseThrow(() -> new RuntimeException(String.format("Could not aggregate input messages: %s", payloads)));
		
		TemplatedMailchimpMessage outputMessage = new TemplatedMailchimpMessage();
		outputMessage.setBody(aggregatedMailchimpContent);
		outputMessage.setReceivingEndpoints(payloads.get(0).getReceivingEndpoints());
		return outputMessage;
	}
	
	private TemplatedMailchimpContent concatContents(TemplatedMailchimpContent one, TemplatedMailchimpContent other) {
		TemplatedMailchimpContent aggregatedContent = new TemplatedMailchimpContent();
		aggregatedContent.setTemplateName(mailchimpSenderConfigProperties.getAggregatedMessageTemplateName());
		
		Map<String, String> templateContent = new HashMap<>(one.getTemplateContent());
		templateContent.putAll(other.getTemplateContent());
		aggregatedContent.setTemplateContent(templateContent);
		
		aggregatedContent.setSubject(mailchimpSenderConfigProperties.getAggregatedMessageSubject());
		
		List<Attachment> dtos = new ArrayList<>(one.getAttachments());
		dtos.addAll(other.getAttachments());
		aggregatedContent.setAttachments(dtos);
		
		Map<String, Object> globalMergeCategoryValues = new HashMap<>();
		mailchimpSenderConfigProperties.getMessageTypes().forEach(type -> globalMergeCategoryValues.put(type, new ArrayList<>()));
		
		aggregateGlobalMergeVariables(one, globalMergeCategoryValues);
		aggregateGlobalMergeVariables(other, globalMergeCategoryValues);
		aggregatedContent.setMergeVariables(globalMergeCategoryValues);
		
		return aggregatedContent;
	}
	
	private void aggregateGlobalMergeVariables(TemplatedMailchimpContent content, Map<String, Object> globalMergeCategoryValues) {
		content.getMergeVariables()
				.forEach((varKey, varValue) -> ((List) globalMergeCategoryValues.get(varKey)).add(varValue));
	}

}