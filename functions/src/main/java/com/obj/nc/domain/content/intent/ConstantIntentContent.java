package com.obj.nc.domain.content.intent;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.sms.SimpleTextContent;
import com.obj.nc.domain.deliveryOptions.ChannelDeliveryOption.CHANNEL_TYPE;
import com.obj.nc.domain.deliveryOptions.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;

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
public class ConstantIntentContent extends BaseIntentContent {

	public final static String JSON_TYPE_IDENTIFIER = "CONSTANT_INTENT_CONTENT";

	@NotNull
	private String body;
	private String subject;
	private List<Attachement> attachments;
	
	@Builder.Default
	private String contentType=MediaType.TEXT_PLAIN_VALUE;

	@Override
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	@Override
	public Content createMessageContent(CHANNEL_TYPE type) {
		if (type == CHANNEL_TYPE.EMAIL) {
			EmailContent emailContent = EmailContent.builder()
				.text(getBody())
				.subject(getSubject())
				.contentType(getContentType())
				.attachments(getAttachments())
				.build();
			
			return emailContent;
		} else if (type == CHANNEL_TYPE.SMS) {
			SimpleTextContent smsContent = SimpleTextContent.builder()
					.text(getBody())
					.build();
			
			return smsContent;
		} else {
			throw new NotImplementedException();
		}
	}


}
