package com.obj.nc.domain.content;

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
@JsonTypeName(AggregatedEmail.JSON_TYPE_IDENTIFIER)
public class AggregatedEmail extends EmailContent {
	
	public final static String JSON_TYPE_IDENTIFIER = "AGGREGATED_EMAIL_MESSAGE_CONTENT";

	//TODO: rename to parts
    private List<EmailContent> aggregateContent = new ArrayList<>();
    
    public void add(EmailContent other) {
    	aggregateContent.add(other);
        getAttachments().addAll(other.getAttachments());
    }
    
    public EmailContent asSimpleContent() {
    	Optional<EmailContent> simpleContent = aggregateContent.stream().reduce(EmailContent::concat);
		return simpleContent
				.orElseThrow(() -> new IllegalStateException("Failed to build message content"));
    }

}
