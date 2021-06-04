package com.obj.nc.domain.notifIntent.content;

import java.util.List;
import java.util.Locale;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.content.sms.TemplateWithModelSmsContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(TemplatedIntentContent.JSON_TYPE_IDENTIFIER)
public class TemplatedIntentContent<MODEL_TYPE> extends IntentContent {
	
	public final static String JSON_TYPE_IDENTIFIER = "TEMPLATED_INTENT_CONTENT";

	@NotNull
	private String templateFileName;

	private String subjectResourceKey;	
	
	private List<Locale> locales;

	@JsonTypeInfo(use = Id.CLASS)
	private MODEL_TYPE model;	
	
	@Override
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public MessageContent createMessageContent(RecievingEndpoint endpoint) {
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
			TemplateWithModelSmsContent smsContent = new TemplateWithModelSmsContent();
				
			smsContent.setTemplateFileName(getTemplateFileName());
			smsContent.setRequiredLocales(getLocales());
			smsContent.setModel(getModel());
				
			return smsContent;
		} else {
			throw new NotImplementedException();
		}
	}

	
}
