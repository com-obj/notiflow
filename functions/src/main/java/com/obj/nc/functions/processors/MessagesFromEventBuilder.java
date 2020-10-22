package com.obj.nc.functions.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.Header;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;

@Configuration
@Log4j2
public class MessagesFromEventBuilder {
	
	private @Autowired GenerateMessagesFromEvent fn;
	
	@Bean
	public Function<Flux<Event>, Flux<Message>> generateMessagesFromEvent() {
		return eventFlux -> eventFlux
				.flatMap(event-> Flux.fromIterable(fn.apply(event)));
	}
	
	@Component
	public static class GenerateMessagesFromEvent implements Function<Event, List<Message>> {

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
}
