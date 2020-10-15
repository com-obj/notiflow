package com.obj.nc.functions.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.domain.Body;
import com.obj.nc.domain.Header;
import com.obj.nc.domain.Recipient;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class CreateMessagesFromEvent {

	@Bean
	public Function<Event, Flux<Message>> generateMessagesFromEvent() {
		return CreateMessagesFromEvent::generateMessagesFromEventAsFlux;
	}

	public static Flux<Message> generateMessagesFromEventAsFlux(Event event) {
		List<Message> messages = generateMessagesFromEvent(event);

		return Flux.fromIterable(messages);
	}

	private static List<Message> generateMessagesFromEvent(Event event) {
		log.debug("Create messages for {}",  event);
		
		List<Message> messages = new ArrayList<Message>();
		for (Recipient recipient: event.getBody().getRecipients()) {
			
			Message msg = new Message();
			msg.stepStart("CreateMessagesFromEvent");
			
			Header msgHeader = msg.getHeader();
			msgHeader.copyHeaderFrom(event.getHeader());
			msgHeader.generateAndSetID();
			
			Body msgBody = msg.getBody();
			Body eventBody = event.getBody();
			msgBody.addRecipient(recipient);
			
			if (recipient.getDeliveryOptions()!=null) {
				msgBody.setDeliveryOptions(recipient.getDeliveryOptions());
			} else {
				msgBody.setDeliveryOptions(eventBody.getDeliveryOptions());
			}
			
			msgBody.setAttachments(eventBody.getAttachments());
			
			msgBody.setMessage(eventBody.getMessage());

			msg.stepFinish();
			
			messages.add(msg);
		}
		return messages;
	}
}
