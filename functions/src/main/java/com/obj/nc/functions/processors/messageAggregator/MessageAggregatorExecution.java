package com.obj.nc.functions.processors.messageAggregator;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.Messages;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.AggregatedEmail;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MessageAggregatorExecution implements Function<Messages, Message> {

//	@DocumentProcessingInfo("AggregateMessages")
	@Override
	public Message apply(Messages messages) {
		Message outputMessage = Message.createAsAggregatedEmail();
		outputMessage.getBody().setRecievingEndpoints(messages.getMessages().get(0).getBody().getRecievingEndpoints());
		outputMessage.getBody().setDeliveryOptions(messages.getMessages().get(0).getBody().getDeliveryOptions());
		
		AggregatedEmail aggregatedContent = outputMessage.getContentTyped();
		for (Message msg: messages.getMessages()) {
			aggregatedContent.add(msg.getContentTyped());
		}

		return outputMessage;
	}

}