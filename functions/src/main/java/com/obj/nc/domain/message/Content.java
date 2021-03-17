package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.BaseJSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = Email.class)
@JsonSubTypes({ 
	@Type(value = Email.class, name = Email.JSON_TYPE_IDENTIFIER),
	@Type(value = AggregatedEmail.class, name = AggregatedEmail.JSON_TYPE_IDENTIFIER),
	@Type(value = SimpleText.class, name = SimpleText.JSON_TYPE_IDENTIFIER),
	@Type(value = EmailFromTemplate.class, name = EmailFromTemplate.JSON_TYPE_IDENTIFIER),
})
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public abstract class Content extends BaseJSONObject {
	
}
