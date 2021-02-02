package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.message.Message;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Log4j2
public class MessageAggregatorExecution implements Function<List<Message>, Message> {

	@DocumentProcessingInfo("AggregateMessages")
	@Override
	public Message apply(List<Message> messages) {
		Message aggregateMessage = new Message();
		// TODO dekoracia Headra
		aggregateMessage.setHeader(messages.get(0).getHeader());

		Body aggregateBody = new Body();
		// TODO message
		aggregateBody.setMessage(messages.get(0).getBody().getMessage());
		// TODO attachments
		aggregateBody.setAttachments(messages.get(0).getBody().getAttachments());
		// TODO attributes
		aggregateBody.setAttributes(messages.get(0).getBody().getAttributes());

		List<RecievingEndpoint> aggregateEndpoints = messages.stream()
				.flatMap(message -> message.getBody().getRecievingEndpoints().stream())
				.collect(Collectors.toList());

		aggregateBody.setRecievingEndpoints(aggregateEndpoints);
		// TODO delivery options
		aggregateBody.setDeliveryOptions(messages.get(0).getBody().getDeliveryOptions());

		return aggregateMessage;
	}

}