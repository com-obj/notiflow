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

package com.obj.nc.domain.notifIntent.content;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.Attachment;
import com.obj.nc.domain.BaseDynamicAttributesBean;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.push.PushContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.endpoints.push.PushEndpoint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.MediaType;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, defaultImpl = IntentContent.class)
@JsonSubTypes({ 
	@Type(value = IntentContent.class, name = IntentContent.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplatedIntentContent.class, name = TemplatedIntentContent.JSON_TYPE_IDENTIFIER),
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class IntentContent extends BaseDynamicAttributesBean {

	public final static String JSON_TYPE_IDENTIFIER = "CONSTANT_INTENT_CONTENT";

	@NotNull
	private String body;
	private String subject;

	private List<Attachment> attachments = new ArrayList<>();
	
	private String contentType=MediaType.TEXT_PLAIN_VALUE;
	
	public static IntentContent createStaticContent(String subject, String body) {
		IntentContent newContent = new IntentContent();
		newContent.setSubject(subject);
		newContent.setBody(body);
		return newContent;
	}

	public static IntentContent createStaticContent(String subject, String body, Attachment...attachments) {
		IntentContent newContent = createStaticContent(subject, body);
		newContent.setAttachments(Arrays.asList(attachments));
		return newContent;
	}

	
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	public MessageContent createMessageContent(ReceivingEndpoint endpoint) {
		if (endpoint instanceof EmailEndpoint) {
			EmailContent emailContent = EmailContent.builder()
				.text(getBody())
				.subject(getSubject())
				.contentType(getContentType())
				.attachments(getAttachments())
				.build();
			
			return emailContent;
		} else if (endpoint instanceof SmsEndpoint) {
			SimpleTextContent smsContent = SimpleTextContent.builder()
					.text(getBody())
					.build();
			
			return smsContent;
		} else if (endpoint instanceof MailchimpEndpoint) {
			throw new NotImplementedException();
//			List<MailchimpAttachmentDto> mailchimpAttachmentDtos = MailchimpAttachmentDto.fromAttachements(attachments);
//			
//			//TODO: add missing pieces
//			MailchimpContent smsContent = MailchimpContent.builder()
//					.subject(getSubject())
//					.attachments(mailchimpAttachmentDtos)
//					.templateName(getBody())
//					.build();
//			
//			return smsContent;
		} else if (endpoint instanceof PushEndpoint) {
			return PushContent
					.builder()
					.subject(getSubject())
					.text(getBody())
					.build();
		} else {
			throw new NotImplementedException();
		}
	}

}
