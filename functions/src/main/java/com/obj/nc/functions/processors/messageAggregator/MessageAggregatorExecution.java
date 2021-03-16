package com.obj.nc.functions.processors.messageAggregator;

import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.Messages;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContentAggregated;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MessageAggregatorExecution implements Function<Messages, Message> {

//	@DocumentProcessingInfo("AggregateMessages")
	@Override
	public Message apply(Messages messages) {
		Message outputMessage = new Message();
		outputMessage.getBody().setRecievingEndpoints(messages.getMessages().get(0).getBody().getRecievingEndpoints());
		outputMessage.getBody().setDeliveryOptions(messages.getMessages().get(0).getBody().getDeliveryOptions());
		
		MessageContentAggregated aggregatedContent = new MessageContentAggregated();
		for (Message msg: messages.getMessages()) {
			aggregatedContent.add(msg.getBody().getMessage());
		}

		outputMessage.getBody().setMessage(aggregatedContent);
		return outputMessage;
	}

}