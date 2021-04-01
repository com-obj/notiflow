package com.obj.nc.domain.content.sms;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(AggregatedSmsContent.JSON_TYPE_IDENTIFIER)
public class AggregatedSmsContent extends SimpleTextContent {
	
	public final static String JSON_TYPE_IDENTIFIER = "AGGREGATED_SMS_MESSAGE_CONTENT";

    private List<SimpleTextContent> aggregateContent = new ArrayList<>();
    
    public void add(SimpleTextContent other) {
    	aggregateContent.add(other);
    }
    
    public SimpleTextContent asSimpleContent() {
    	Optional<SimpleTextContent> simpleContent = aggregateContent.stream().reduce(SimpleTextContent::concat);
		return simpleContent
				.orElseThrow(() -> new IllegalStateException("Failed to build message content"));
    }

}
