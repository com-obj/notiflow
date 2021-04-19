package com.obj.nc.domain.content.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.IsAggregatedContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(AggregatedEmailContent.JSON_TYPE_IDENTIFIER)
public class AggregatedEmailContent extends EmailContent implements IsAggregatedContent<EmailContent> {
	
	public final static String JSON_TYPE_IDENTIFIER = "AGGREGATED_EMAIL_MESSAGE_CONTENT";

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
