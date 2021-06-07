package com.obj.nc.domain.content;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.content.email.EmailContent;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = EmailContent.class)
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class MessageContent extends BaseJSONObject {
	
}
