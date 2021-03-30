package com.obj.nc.functions.processors.messageBuilder;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.Header;
import com.obj.nc.domain.endpoints.*;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
@Log4j2
public class MessagesFromEventExecution implements Function<NotificationIntent, List<Message>> {

	@DocumentProcessingInfo("GenerateMessagesFromEvent")
	@Override
	public List<Message> apply(NotificationIntent notificationIntent) {
		log.debug("Create messages for {}",  notificationIntent);

		List<Message> messages = new ArrayList<Message>();

		for (RecievingEndpoint recievingEndpoint: notificationIntent.getBody().getRecievingEndpoints()) {

			Message msg = new Message();
			msg.setProcessingInfo(notificationIntent.getProcessingInfo());
			msg.stepStart("CreateMessagesFromEvent");

			Header msgHeader = msg.getHeader();
			msgHeader.copyHeaderFrom(notificationIntent.getHeader());
			msgHeader.generateAndSetID();

			Body msgBody = msg.getBody();
			Body eventBody = notificationIntent.getBody();
			msgBody.addRecievingEndpoints(recievingEndpoint);

			if (recievingEndpoint.getDeliveryOptions()!=null) {
				msgBody.setDeliveryOptions(recievingEndpoint.getDeliveryOptions());
				recievingEndpoint.setDeliveryOptions(null);
			} else {
				msgBody.setDeliveryOptions(eventBody.getDeliveryOptions());
			}

			msgBody.setAttributes(eventBody.getAttributes());
			
			msgBody.setMessage(eventBody.getMessage());
			msgBody.getMessage().setAttributes(eventBody.getMessage().getAttributes());

			msg.stepFinish();

			messages.add(msg);
		}

		return messages;
	}
}