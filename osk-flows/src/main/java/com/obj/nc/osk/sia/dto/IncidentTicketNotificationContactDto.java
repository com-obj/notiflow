package com.obj.nc.osk.sia.dto;

import java.util.List;

/**
 * Don't change, this is part of communication protocol with SIA application
 * @author ja
 *
 */
public class IncidentTicketNotificationContactDto {
	Long id;
	String name;
	List<String> emails;
	List<String> phones;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public List<String> getPhones() {
		return phones;
	}

	public void setPhones(List<String> phones) {
		this.phones = phones;
	}
}
