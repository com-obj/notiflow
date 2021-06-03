package com.obj.nc.domain.notifIntent.content;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Builder
@JsonTypeName(ConstantIntentContent.JSON_TYPE_IDENTIFIER)
/**
 * This is usefull when you need to set notification but you don't know via
 * which channel it will be send because it might be based on user setting
 * (DeliveryOptions)
 * 
 * @author i76700
 *
 */
public class ConstantIntentContent extends IntentContent {

	public final static String JSON_TYPE_IDENTIFIER = "CONSTANT_INTENT_CONTENT";

	@NotNull
	private String body;
	private String subject;

	@Builder.Default
	private List<Attachement> attachments = new ArrayList<>();
	
	@Builder.Default
	private String contentType=MediaType.TEXT_PLAIN_VALUE;

	@Override
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	@Override
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
		} else {
			throw new NotImplementedException();
		}
	}


}
