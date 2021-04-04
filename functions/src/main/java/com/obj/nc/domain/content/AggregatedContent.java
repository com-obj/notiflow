package com.obj.nc.domain.content;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(AggregatedContent.JSON_TYPE_IDENTIFIER)
public class AggregatedContent extends Content {
	
	public final static String JSON_TYPE_IDENTIFIER = "AGGREGATED_MESSAGE_CONTENT";

    private List<Content> aggregateContent = new ArrayList<>();
    
    public void add(Content other) {
    	aggregateContent.add(other);
    }

}
