package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Attachement;
import com.obj.nc.domain.BasePayload;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Component;

import java.util.*;
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

		MessageContent messageContent = aggregateBodyMessageContent(messages);
		aggregateBody.setMessage(messageContent);

		List<Attachement> aggregateAttachments = aggregateBodyAttachments(messages);
		aggregateBody.setAttachments(aggregateAttachments);

		Map<String, Object> aggregatedBodyAttributes = aggregateBodyAttributes(messages);
		aggregateBody.setAttributes(aggregatedBodyAttributes);

		aggregateBody.setRecievingEndpoints(messages.get(0).getBody().getRecievingEndpoints());
		aggregateBody.setDeliveryOptions(messages.get(0).getBody().getDeliveryOptions());

		aggregateMessage.setBody(aggregateBody);
		return aggregateMessage;
	}

	private List<Attachement> aggregateBodyAttachments(List<Message> messages) {
		return messages.stream()
				.flatMap(message -> message.getBody().getAttachments().stream())
				.collect(Collectors.toList());
	}

	private Map<String, Object> aggregateBodyAttributes(List<Message> messages) {
		Map<String, Object> aggregatedBodyAttributes = new HashMap<>();

		messages.stream()
				.flatMap(message -> message.getBody().getAttributes().entrySet().stream())
				.forEach(entry -> aggregatedBodyAttributes.putIfAbsent(entry.getKey(), entry.getValue()));

		return aggregatedBodyAttributes;
	}

	private MessageContent aggregateBodyMessageContent(List<Message> messages) {
		List<String> messageTexts = messages.stream()
				.map(BasePayload::getBody)
				.map(Body::getMessage)
				.map(MessageContent::getText)
				.collect(Collectors.toList());

		List<String> messageSubjects = messages.stream()
				.map(BasePayload::getBody)
				.map(Body::getMessage)
				.map(MessageContent::getSubject)
				.collect(Collectors.toList());

		Optional<MessageContent> aggregateMessageContent = StreamEx.ofReversed(messageTexts)
				.zipWith(StreamEx.ofReversed(messageSubjects), MessageContent::new)
				// add child message to parent and return parent
				.reduce((m1, m2) -> m2.setNextMessage(m1));

		return aggregateMessageContent.orElseThrow(() -> new IllegalStateException("Failed to Aggregate message content"));
	}

}