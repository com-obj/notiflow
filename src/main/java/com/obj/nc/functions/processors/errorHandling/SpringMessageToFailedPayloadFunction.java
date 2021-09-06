package com.obj.nc.functions.processors.errorHandling;

import java.util.ListIterator;
import java.util.Properties;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.history.MessageHistory;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.obj.nc.config.SpringIntegration;
import com.obj.nc.flows.errorHandling.domain.FailedPayload;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SpringMessageToFailedPayloadFunction extends ProcessorFunctionAdapter<ErrorMessage, FailedPayload> {
	
	@Autowired @Qualifier(SpringIntegration.OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME) ObjectMapper jsonConverterForMessages;
	
	@Override
	protected FailedPayload execute(ErrorMessage errorMessage) {	
		try {
			MessagingException exMsg =  (MessagingException)errorMessage.getPayload();
			Throwable exception = exMsg.getCause();
			
			String lastChannelName = extractLastChannelName(exMsg);
			
			Message<?> failedMessageForDb = MessageBuilder
				.withPayload(exMsg.getFailedMessage().getPayload())
				.filterAndCopyHeadersIfAbsent(
							exMsg.getFailedMessage().getHeaders(), 
							MessageHistory.HEADER_NAME, MessageHeaders.REPLY_CHANNEL, MessageHeaders.ERROR_CHANNEL) //cannot de-serialize, need to remove. 
				.build();
			
			JsonNode jsonTree = jsonConverterForMessages.valueToTree(failedMessageForDb);
			
			FailedPayload failedPaylod = FailedPayload.builder()
					.messageJson( jsonTree )
					.channelNameForRetry(lastChannelName)
					.flowId("TODO")
					.id(UUID.randomUUID())
					.build();
			
			failedPaylod.setAttributesFromException(exception);
			
			return failedPaylod;
		} catch (Exception e) {
			log.error("Error ocured in Error handling flow: ", e);
			throw new RuntimeException (e);
		}
	}

	protected String extractLastChannelName(MessagingException exMsg) {
		MessageHistory hitoryEntries = MessageHistory.read(exMsg.getFailedMessage());
		ListIterator<Properties> listIterator = hitoryEntries.listIterator(hitoryEntries.size());

		String lastChannelName = null;
		while (listIterator.hasPrevious()) {
		    Properties entry = listIterator.previous();
		    String typeName = (String)entry.get(MessageHistory.TYPE_PROPERTY);
		    
		    if (("publish-subscribe-channel".equals(typeName))
		    	||
		    	("channel".equals(typeName))) {
		    	
			    lastChannelName = entry.getProperty(MessageHistory.NAME_PROPERTY);
			    break;
		    }
		}
		
		return lastChannelName;
	}


}
