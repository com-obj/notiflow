package com.obj.nc.osk.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Don't change, this is part of communication protocol with SIA application
 * @author ja
 *
 */
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class IncidentTicketNotificationContactDto {
	Long id;
	String name;
	List<String> emails;
	List<String> phones;
	
	public Set<EmailEndpoint> asEmailEnpoints() {
		Set<EmailEndpoint> emails = new HashSet<>();
		
		Person customer = new Person(name);
		
		for (String email: this.emails) {
			EmailEndpoint customerEmail = EmailEndpoint.createForPerson(customer,email);
			emails.add(customerEmail);
		}
		
		return emails;
	}
}
