package com.obj.nc.domain.notifIntent.content;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.content.MessageContent;
import com.obj.nc.domain.endpoints.RecievingEndpoint;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, defaultImpl = ConstantIntentContent.class)
@JsonSubTypes({ 
	@Type(value = ConstantIntentContent.class, name = ConstantIntentContent.JSON_TYPE_IDENTIFIER),
	@Type(value = TemplatedIntentContent.class, name = TemplatedIntentContent.JSON_TYPE_IDENTIFIER),
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class IntentContent extends BaseJSONObject {

	@JsonIgnore
	public abstract String getContentTypeName();
	
	public abstract MessageContent createMessageContent(RecievingEndpoint endpoint);

}
