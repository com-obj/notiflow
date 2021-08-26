package com.obj.nc.functions.processors.messageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("GenerateMessagesFromIntent")
public class MessagesFromIntentGenerator extends ProcessorFunctionAdapter<NotificationIntent, List<? extends Message<?>>> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent notificationIntent) {

		if (notificationIntent.getRecievingEndpoints().isEmpty()) {
			return Optional.of(new PayloadValidationException(
					String.format("NotificationIntent %s has no receiving endpoints defined.", notificationIntent)));
		}

		return Optional.empty();
	}

	@Override
	protected List<? extends Message<?>> execute(NotificationIntent notificationIntent) {
		log.debug("Create messages for {}",  notificationIntent);
		
		// TODO different settings can apply here based on delivery options like if we are outside business hours, convert to email. otherwise convert to SMS
		List<Message<?>> messages = new ArrayList<>();
		

		for (RecievingEndpoint recievingEndpoint: notificationIntent.getRecievingEndpoints()) {
			
			Message<?> msg = (Message<?>) notificationIntent.createMessage(recievingEndpoint);
			
			msg.addRecievingEndpoints(recievingEndpoint);

			msg.setAttributes(notificationIntent.getAttributes());
			
			notificationIntent.getEventIds().forEach(msg::addEventId);
			notificationIntent.getIntentIds().forEach(msg::addIntentId);
			
			messages.add(msg);
		}

		return messages;
	}

}
