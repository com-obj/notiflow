package com.obj.nc.domain;

import java.util.List;

import com.obj.nc.domain.endpoints.DeliveryOptions.AGGREGATION_TYPE;
import com.obj.nc.domain.message.Message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = false)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@AllArgsConstructor
@Deprecated
public class Messages extends BaseJSONObject {

    private List<Message> messages;
    
    public boolean isAllDeliveryOptionAggregated() {
    	return messages
    				.stream()
    				.allMatch(msg -> msg.getBody().getDeliveryOptions().getAggregationType() != AGGREGATION_TYPE.NONE);
    }
    
    public Message getFirst() {
    	return messages.iterator().next();
    }
    
    public boolean isEmpty() {
    	return messages.isEmpty();
    }

}
