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
public class AggregatedEmail extends Email {
	
	public final static String JSON_TYPE_IDENTIFIER = "AGGREGATED_EMAIL_MESSAGE_CONTENT";

	//TODO: rename to parts
    private List<Email> aggregateContent = new ArrayList<>();

    public AggregatedEmail from(Email other) {
        AggregatedEmail aggregated = new AggregatedEmail();
        aggregated.setSubject(other.getSubject());
        aggregated.setText(other.getText());
        aggregated.setAttachments(other.getAttachments());
        return aggregated;
    }
    
    public void add(Email other) {
    	aggregateContent.add(other);
        getAttachments().addAll(other.getAttachments());
    }
    
    public Email asSimpleContent() {
    	Optional<Email> simpleContent = aggregateContent.stream().reduce(Email::concat);
		return simpleContent
				.orElseThrow(() -> new IllegalStateException("Failed to build message content"));
    }

}
