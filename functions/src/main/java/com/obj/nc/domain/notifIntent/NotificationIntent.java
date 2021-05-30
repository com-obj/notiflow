package com.obj.nc.domain.notifIntent;

import java.util.List;

import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.content.email.EmailContent;
import com.obj.nc.domain.content.intent.BaseIntentContent;
import com.obj.nc.domain.deliveryOptions.ChannelDeliveryOption;
import com.obj.nc.domain.deliveryOptions.DeliveryOptions;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.EmailMessage;
import com.obj.nc.domain.message.Message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Data
@NoArgsConstructor
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Table("nc_intent")
@Log4j2
public class NotificationIntent<BODY_TYPE extends BaseIntentContent> extends BasePayload<BODY_TYPE> {
	
	public static final String JSON_TYPE_IDENTIFIER = "INTENT";
	
	@Override
	@JsonIgnore
	public String getPayloadTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}

	public Message<?> createMessage(List<RecievingEndpoint> endpointsForOneSubject, DeliveryOptions options) {
		// TODO different settings can apply here like if we are outside business hours, convert to email. otherwise convert to SMS
		// this implementation is currently first draft
		
		if (options.getChannel().getChannelType() == ChannelDeliveryOption.CHANNEL_TYPE.EMAIL) {
			EmailMessage msg = new EmailMessage();
			Content msgContent = getBody().createMessageContent(ChannelDeliveryOption.CHANNEL_TYPE.EMAIL);
			msg.setBody((EmailContent)msgContent);
			
		}
		return null;

	}

	
}
