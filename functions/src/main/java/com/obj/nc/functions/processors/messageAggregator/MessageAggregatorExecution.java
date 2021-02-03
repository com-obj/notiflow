package com.obj.nc.functions.processors.messageAggregator;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.MessageContent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Component
@Log4j2
public class MessageAggregatorExecution implements Function<List<Message>, Message> {

	private static final String AGGREGATE_MESSAGE_SUBJECT = "Digest subject";
	private static final String AGGREGATE_MESSAGE_TEXT = "Digest text";

	@DocumentProcessingInfo("AggregateMessages")
	@Override
	public Message apply(List<Message> messages) {
		List<Message> messagesReversed = new ArrayList<>(messages);
		Collections.reverse(messagesReversed);

		return messagesReversed.stream()
				.reduce((message, other) -> other.merge(message))
				.map(message -> message.wrapMessageContent(new MessageContent(AGGREGATE_MESSAGE_TEXT, AGGREGATE_MESSAGE_SUBJECT)))
				.orElseThrow(() -> new IllegalStateException("Failed to Aggregate messages"));
	}

}