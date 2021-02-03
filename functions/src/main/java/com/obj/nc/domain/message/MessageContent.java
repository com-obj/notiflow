package com.obj.nc.domain.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.event.Event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class MessageContent extends BaseJSONObject {

	@NonNull
	@EqualsAndHashCode.Include
	private String text;

	@NonNull
	@EqualsAndHashCode.Include
	private String subject;

	@JsonProperty("message")
	@EqualsAndHashCode.Include
	private MessageContent nextMessage;

	public MessageContent setNextMessage(MessageContent nextMessage) {
		this.nextMessage = nextMessage;
		return this;
	}

}
