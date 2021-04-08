package com.obj.nc.osk.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Don't change, this is part of communication protocol with SIA application
 * @author ja
 *
 */
@Data
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncidentTicketNotificationContactDto {
	Long id;
	String name;
	@Singular
	List<String> emails;
	@Singular
	List<String> phones;
	
	public Set<EmailEndpoint> asEmailEnpoints() {
		Set<EmailEndpoint> emails = new HashSet<>();
		
		Person customer = new Person(name);
		
		for (String email: this.getEmails()) {
			EmailEndpoint emailEndpoint = EmailEndpoint.createForPerson(customer,email);
			emails.add(emailEndpoint);
		}
		
		return emails;
	}

	public Set<SmsEndpoint> asSmsEnpoints() {
		Set<SmsEndpoint> smss = new HashSet<>();
		
		Person customer = new Person(name);
		
		for (String phoneNumber: this.getPhones()) {
			SmsEndpoint smsEndpoint = SmsEndpoint.createForPerson(customer,phoneNumber);
			smss.add(smsEndpoint);
		}
		
		return smss;
	}
	
	public Set<RecievingEndpoint> asEnpoints() {
		Set<RecievingEndpoint> endpoints = new HashSet<>();
		asEmailEnpoints().forEach(email-> endpoints.add(email));
		asSmsEnpoints().forEach(sms-> endpoints.add(sms));
		
		return endpoints;
	}
	
	public List<String> getEmails() {
		if (this.emails == null) {
			this.emails = new ArrayList<String>();
		}

		return emails;
	}
	
	public List<String> getPhones() {
		if (this.phones == null) {
			this.phones = new ArrayList<String>();
		}

		return phones;
	}
}
