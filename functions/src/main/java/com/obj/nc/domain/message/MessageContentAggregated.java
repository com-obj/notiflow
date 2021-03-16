package com.obj.nc.domain.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MessageContentAggregated extends MessageContent {
	
	public final static String JSON_TYPE_IDENTIFIER = "AGGREGATED_MESSAGE_CONTENT";

	//TODO: rename to parts
    private List<MessageContent> aggregateContent = new ArrayList<>();

    public MessageContentAggregated from(MessageContent other) {
        MessageContentAggregated aggregated = new MessageContentAggregated();
        aggregated.setSubject(other.getSubject());
        aggregated.setText(other.getText());
        aggregated.setAttachments(other.getAttachments());
        return aggregated;
    }
    
    public void add(MessageContent other) {
    	aggregateContent.add(other);
        getAttachments().addAll(other.getAttachments());
    }
    
    public MessageContent asSimpleContent() {
    	Optional<MessageContent> simpleContent = aggregateContent.stream().reduce(MessageContent::concat);
		return simpleContent
				.orElseThrow(() -> new IllegalStateException("Failed to build message content"));
    }

}
