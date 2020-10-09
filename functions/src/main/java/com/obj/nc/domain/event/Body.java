package com.obj.nc.domain.event;

import com.obj.nc.domain.message.Message;

public class Body {

	Message message = new Message();

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}
}
