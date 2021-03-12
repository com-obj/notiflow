package com.obj.nc.osk.sia.dto;

import java.util.Date;
import java.util.List;

public class IncidentTicketNotificationEventDto {
	Long id;
	String name;
	String description;
	Date outageStart;
	Date outageEnd;
	List<IncidentTicketNotificationMessageDto> messages;

	public Date getOutageStart() {
		return outageStart;
	}

	public void setOutageStart(Date outageStart) {
		this.outageStart = outageStart;
	}

	public Date getOutageEnd() {
		return outageEnd;
	}

	public void setOutageEnd(Date outageEnd) {
		this.outageEnd = outageEnd;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<IncidentTicketNotificationMessageDto> getMessages() {
		return messages;
	}

	public void setMessages(List<IncidentTicketNotificationMessageDto> messages) {
		this.messages = messages;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
