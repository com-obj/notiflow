package com.obj.nc.functions.processors.messageBuilder;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.Header;
import com.obj.nc.domain.endpoints.*;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
@Log4j2
public class MessagesFromEventExecution implements Function<Event, List<Message>> {

	@DocumentProcessingInfo("GenerateMessagesFromEvent")
	@Override
	public List<Message> apply(Event event) {
		log.debug("Create messages for {}",  event);

		List<Message> messages = new ArrayList<Message>();

		for (RecievingEndpoint recievingEndpoint: event.getBody().getRecievingEndpoints()) {

			Message msg = new Message();
			msg.setProcessingInfo(event.getProcessingInfo());
			msg.stepStart("CreateMessagesFromEvent");

			Header msgHeader = msg.getHeader();
			msgHeader.copyHeaderFrom(event.getHeader());
			msgHeader.generateAndSetID();

			Body msgBody = msg.getBody();
			Body eventBody = event.getBody();
			msgBody.addRecievingEndpoints(recievingEndpoint);

			if (recievingEndpoint.getDeliveryOptions()!=null) {
				msgBody.setDeliveryOptions(recievingEndpoint.getDeliveryOptions());
				recievingEndpoint.setDeliveryOptions(null);
			} else {
				msgBody.setDeliveryOptions(eventBody.getDeliveryOptions());
			}

			msgBody.setAttributes(eventBody.getAttributes());
			
			msgBody.setMessage(eventBody.getMessage());
			msgBody.getMessage().getContent().setAttributes(eventBody.getMessage().getContent().getAttributes());

			msg.stepFinish();

			messages.add(msg);
		}

		return messages;
	}
}