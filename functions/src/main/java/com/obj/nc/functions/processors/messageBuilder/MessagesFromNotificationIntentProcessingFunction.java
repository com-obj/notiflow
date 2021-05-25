package com.obj.nc.functions.processors.messageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.components.api.MessageFactory;
import com.obj.nc.domain.content.Content;
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
public class MessagesFromNotificationIntentProcessingFunction<CONTENT_TYPE extends Content> extends ProcessorFunctionAdapter<NotificationIntent<CONTENT_TYPE>, List<Message<CONTENT_TYPE>>> {

	private final MessageFactory messageFactory;
	
	@Override
	protected Optional<PayloadValidationException> checkPreCondition(NotificationIntent<CONTENT_TYPE> notificationIntent) {

		if (notificationIntent.getRecievingEndpoints().isEmpty()) {
			return Optional.of(new PayloadValidationException(
					String.format("NotificationIntent %s has no receiving endpoints defined.", notificationIntent)));
		}

		return Optional.empty();
	}

	@Override
	protected List<Message<CONTENT_TYPE>> execute(NotificationIntent<CONTENT_TYPE> notificationIntent) {
		log.debug("Create messages for {}",  notificationIntent);

		List<Message<CONTENT_TYPE>> messages = new ArrayList<>();

		for (RecievingEndpoint recievingEndpoint: notificationIntent.getRecievingEndpoints()) {

			Message<CONTENT_TYPE> msg = messageFactory.createBasedOnEndpoint(recievingEndpoint.getClass());
			
			msg.addRecievingEndpoints(recievingEndpoint);

			msg.setAttributes(notificationIntent.getAttributes());
			msg.setBody(notificationIntent.getBody());
			messages.add(msg);
		}

		return messages;
	}

}
