package com.obj.nc.functions.processors.messageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Component
@AllArgsConstructor
@Log4j2
@DocumentProcessingInfo("MessageByRecipientTokenizer")
public class MessageByRecipientTokenizer<CONTENT_TYPE extends MessageContent> extends ProcessorFunctionAdapter<Message<CONTENT_TYPE>, List<Message<CONTENT_TYPE>>> {

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(Message<CONTENT_TYPE> notificationIntent) {

		if (notificationIntent.getRecievingEndpoints().isEmpty()) {
			return Optional.of(new PayloadValidationException(
					String.format("NotificationIntent %s has no receiving endpoints defined.", notificationIntent)));
		}

		return Optional.empty();
	}

	@Override
	@SneakyThrows
	protected List<Message<CONTENT_TYPE>> execute(Message<CONTENT_TYPE> msg) {
		log.debug("Tokenizing message {} for recipients",  msg);

		List<Message<CONTENT_TYPE>> messages = new ArrayList<>();

		for (RecievingEndpoint recievingEndpoint: msg.getRecievingEndpoints()) {

			Message<CONTENT_TYPE> msgClone = Message.newMessageFrom(msg.getClass(), msg);
			
			msgClone.addRecievingEndpoints(recievingEndpoint);

			msgClone.setAttributes(msg.getAttributes());
			msgClone.setBody(msg.getBody());
			
			messages.add(msgClone);
		}

		return messages;
	}
	
}
