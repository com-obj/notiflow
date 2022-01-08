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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.ReceivingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Singular;

import org.apache.commons.lang3.NotImplementedException;

import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.Locale;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonTypeName(TemplatedIntentContent.JSON_TYPE_IDENTIFIER)
@Builder
public class TemplatedIntentContent<MODEL_TYPE> extends IntentContent {
	
	public final static String JSON_TYPE_IDENTIFIER = "TEMPLATED_INTENT_CONTENT";

	@NotNull
	private String templateFileName;

	private String subjectResourceKey;	
	
	@Singular
	private List<Locale> locales;

	@JsonTypeInfo(use = Id.CLASS)
	private MODEL_TYPE model;	
	
	@Override
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public MessageContent createMessageContent(ReceivingEndpoint endpoint) {
		if (endpoint instanceof EmailEndpoint) {
			TemplateWithModelEmailContent emailContent = TemplateWithModelEmailContent.builder()
				.subjectResourceKey(getSubjectResourceKey())
				.attachments(getAttachments())
				.build();
			
			emailContent.setTemplateFileName(getTemplateFileName());
			emailContent.setRequiredLocales(getLocales());
			emailContent.setModel(getModel());
			
			return emailContent;
		} else if (endpoint instanceof SmsEndpoint) {
			TemplateWithModelContent<MODEL_TYPE> smsContent = new TemplateWithModelContent<MODEL_TYPE>();
				
			smsContent.setTemplateFileName(getTemplateFileName());
			smsContent.setRequiredLocales(getLocales());
			smsContent.setModel(getModel());
				
			return smsContent;
		} else {
			throw new NotImplementedException();
		}
	}

	
}
