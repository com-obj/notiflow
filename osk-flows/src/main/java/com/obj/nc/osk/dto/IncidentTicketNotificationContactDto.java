package com.obj.nc.osk.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

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
	List<String> emails = new ArrayList<>();
	List<String> phones = new ArrayList<>();
	
	public Set<EmailEndpoint> asEmailEnpoints() {
		Set<EmailEndpoint> emails = new HashSet<>();
		
		Person customer = new Person(name);
		
		for (String email: this.emails) {
			EmailEndpoint emailEndpoint = EmailEndpoint.createForPerson(customer,email);
			emails.add(emailEndpoint);
		}
		
		return emails;
	}

	public Set<SmsEndpoint> asSmsEnpoints() {
		Set<SmsEndpoint> smss = new HashSet<>();
		
		Person customer = new Person(name);
		
		for (String phoneNumber: this.phones) {
			SmsEndpoint smsEndpoint = SmsEndpoint.createForPerson(customer,phoneNumber);
			smss.add(smsEndpoint);
		}
		
		return smss;
	}
	
	public Set<RecievingEndpoint> asEnpoints() {
		Set<RecievingEndpoint> endpoints = new HashSet<>();
		asEmailEnpoints().forEach(email-> endpoints.add(email));
//		asSmsEnpoints().forEach(sms-> endpoints.add(sms));
		
		return endpoints;
	}
}
