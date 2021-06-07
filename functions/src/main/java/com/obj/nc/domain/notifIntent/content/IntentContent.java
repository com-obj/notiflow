package com.obj.nc.domain.notifIntent.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.MailchimpEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, defaultImpl = IntentContent.class)
@JsonSubTypes({ 
	@Type(value = IntentContent.class, name = IntentContent.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplatedIntentContent.class, name = TemplatedIntentContent.JSON_TYPE_IDENTIFIER),
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class IntentContent extends BaseJSONObject {

	public final static String JSON_TYPE_IDENTIFIER = "CONSTANT_INTENT_CONTENT";

	@NotNull
	private String body;
	private String subject;

	private List<Attachement> attachments = new ArrayList<>();
	
	private String contentType=MediaType.TEXT_PLAIN_VALUE;
	
	public static IntentContent createStaticContent(String subject, String body) {
		IntentContent newContent = new IntentContent();
		newContent.setSubject(subject);
		newContent.setBody(body);
		return newContent;
	}

	public static IntentContent createStaticContent(String subject, String body, Attachement ...attachments) {
		IntentContent newContent = createStaticContent(subject, body);
		newContent.setAttachments(Arrays.asList(attachments));
		return newContent;
	}

	
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	public MessageContent createMessageContent(RecievingEndpoint endpoint) {
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
//			//TODO: add missing peaces
//			MailchimpContent smsContent = MailchimpContent.builder()
//					.subject(getSubject())
//					.attachments(mailchimpAttachmentDtos)
//					.templateName(getBody())
//					.build();
//			
//			return smsContent;
		} else {
			throw new NotImplementedException();
		}
	}

}
