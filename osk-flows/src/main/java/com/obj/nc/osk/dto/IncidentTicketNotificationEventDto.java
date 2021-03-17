package com.obj.nc.osk.dto;

import static com.obj.nc.osk.functions.NotificationEventConverterProcessingFunction.OUTAGE_INFOS_ATTR_NAME;
import static com.obj.nc.osk.functions.NotificationEventConverterProcessingFunction.OUTAGE_START_ATTR_NAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.obj.nc.domain.Body;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Content;
import com.obj.nc.domain.message.Email;
import com.obj.nc.osk.config.StaticRoutingOptions;
import com.obj.nc.osk.dto.IncidentTicketServiceOutageForCustomerDto.CustomerSegment;
import com.obj.nc.osk.functions.NotificationEventConverterProcessingFunction;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class IncidentTicketNotificationEventDto {
	Long id;
	String name;
	String description;
	Date outageStart;
	Date outageEnd;
	List<IncidentTicketServiceOutageForCustomerDto> messages;
	
	public List<Event> createCustomersEvents() {
		List<Event> customerEvents = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> outageForCustomers = groupByCustomerPerson();
		
		for (IncidentTicketNotificationContactDto customer: outageForCustomers.keySet()) {
			Event event  = createCustomerEvent(customer, outageForCustomers.get(customer));
			customerEvents.add(event);
		}
		return customerEvents;
	} 
	
	private Event createCustomerEvent(IncidentTicketNotificationContactDto customer, List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer) {
		Email customerMessageContent = new Email();
		customerMessageContent.putAttributeValue(OUTAGE_START_ATTR_NAME, getOutageStart());
		customerMessageContent.setSubject("Vase sluzby mozu byt nedostupne");
		customerMessageContent.setText("Vážený zákazník, o xx:xx hod. sme zaznamenali výpadok..."); 
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutagesForCustomer);
		customerMessageContent.putAttributeValue(OUTAGE_INFOS_ATTR_NAME, outageInfos);

		Set<EmailEndpoint> customerEmails = customer.asEmailEnpoints();
		
		Body eventBody = new Body();
		eventBody.setMessage(customerMessageContent);
		eventBody.getRecievingEndpoints().addAll(customerEmails);
		
		Event event = new Event();
		event.setBody(eventBody);
		
		return event;
	}	
	
	private Set<IncidentTicketNotificationContactDto> extractDistinctSalesContacts() {
		return messages.stream()
					.map(outage -> outage.getSellerContacts())
					.flatMap(contacts -> contacts.stream())
					.collect(Collectors.toSet());
	}
	
	private Set<IncidentTicketNotificationContactDto> extractDistinctCustomerContacts() {
		return messages.stream()
					.map(outage -> outage.getCustomerContacts())
					.flatMap(contacts -> contacts.stream())
					.collect(Collectors.toSet());
	}

	private List<ServiceOutageInfo> convertToServiceOutages(List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer) {
		List<ServiceOutageInfo> outageInfos = new ArrayList<>();
		
		for (IncidentTicketServiceOutageForCustomerDto serviceOutage : serviceOutagesForCustomer) {
			ServiceOutageInfo outage = ServiceOutageInfo.from(serviceOutage);
			
			outageInfos.add(outage);
		}
		
		return outageInfos;
	}

	
	public void filterOutLAsNotInConfig(StaticRoutingOptions config) {
		messages = 
			messages.stream()
				.filter(msg -> 
							msg.getCustomerSegment() == CustomerSegment.SME
							||
							(msg.getCustomerSegment() == CustomerSegment.LA 
								&& 
							config.getB2bLoginOfLACustumersToBeNotified().contains(msg.getB2bLogin()))
				)
			.collect(Collectors.toList());
	}

	public List<Event> createSalesEvents() {
		List<Event> salesEvents = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidentsForSellers = groupBySalesPerson();
		
		for (IncidentTicketNotificationContactDto salesContact: incidentsForSellers.keySet()) {
			Event event  = createSalesEvent(salesContact, incidentsForSellers.get(salesContact));
			salesEvents.add(event);
		}
		return salesEvents;
	}
	
	private Event createSalesEvent(IncidentTicketNotificationContactDto salesContact, List<IncidentTicketServiceOutageForCustomerDto> serviceOutages) {
		Email customerMessageContent = new Email();
		customerMessageContent.putAttributeValue("outageStart", getOutageStart());
		customerMessageContent.setSubject("Tvoji zakaznici maju problem");
		customerMessageContent.setText("Ahoj, zákazník //xxxx// ma pravdepodobne problém. Informovali sme ho o tom správou."); 
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutages);
		customerMessageContent.putAttributeValue("outageInfos", outageInfos);

		Set<EmailEndpoint> customerEmails = salesContact.asEmailEnpoints();
		
		Body eventBody = new Body();
		eventBody.setMessage(customerMessageContent);
		eventBody.getRecievingEndpoints().addAll(customerEmails);
		
		Event event = new Event();
		event.setBody(eventBody);
		
		return event;
	}	

	private Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> groupBySalesPerson() {
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidentsForSellers = new HashMap<>();
		
		Set<IncidentTicketNotificationContactDto> salesContacts = extractDistinctSalesContacts();
		
		for (IncidentTicketNotificationContactDto salesContact: salesContacts) {
			List<IncidentTicketServiceOutageForCustomerDto> incidentsForSaller = 
				messages.stream()
					.filter(msg -> msg.getSellerContacts().contains(salesContact))
					.collect(Collectors.toList());
			
			incidentsForSellers.put(salesContact, incidentsForSaller);
		}
		
		return incidentsForSellers;
	}
	
	//copy/paste groupBySalesPerson
	private Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> groupByCustomerPerson() {
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidetnsForCustomerContact = new HashMap<>();
		
		Set<IncidentTicketNotificationContactDto> customerContacts = extractDistinctCustomerContacts();
		
		for (IncidentTicketNotificationContactDto customerContact: customerContacts) {
			List<IncidentTicketServiceOutageForCustomerDto> incidentsForCustomer = 
				messages.stream()
					.filter(msg -> msg.getCustomerContacts().contains(customerContact))
					.collect(Collectors.toList());
			
			incidetnsForCustomerContact.put(customerContact, incidentsForCustomer);
		}
		
		return incidetnsForCustomerContact;
	}

	
}
