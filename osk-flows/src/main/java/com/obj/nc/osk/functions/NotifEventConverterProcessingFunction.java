package com.obj.nc.osk.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.osk.dto.IncidentTicketNotificationContactDto;
import com.obj.nc.osk.dto.IncidentTicketNotificationEventDto;
import com.obj.nc.osk.dto.IncidentTicketServiceOutageForCustomerDto;
import com.obj.nc.osk.dto.IncidentTicketServiceOutageForCustomerDto.CustomerSegment;
import com.obj.nc.osk.functions.content.CustEventStartEmailTemplate;
import com.obj.nc.osk.functions.content.SalesEventStartEmailTemplate;
import com.obj.nc.osk.functions.model.CustEventStartModel;
import com.obj.nc.osk.functions.model.SalesEventStartModel;
import com.obj.nc.osk.functions.model.ServiceOutageInfo;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class NotifEventConverterProcessingFunction extends ProcessorFunctionAdapter<GenericEvent, List<Event>> {
	
	private NotifEventConverterConfig config;
	
	public static String OUTAGE_START_ATTR_NAME = "outageStart";
	public static String OUTAGE_INFOS_ATTR_NAME = "outageInfos";

	@Override
	protected Optional<PayloadValidationException> checkPreCondition(GenericEvent payload) {
		if (payload == null) {
			return Optional.of(new PayloadValidationException("GenericEvent must not be null"));
		}
		
		if (payload.getPayloadJson()==null) {
			return Optional.of(new PayloadValidationException("GenericEvent doesn't contain original message"));
		}

		return Optional.empty();
	}

	@Override
	@DocumentProcessingInfo
	protected List<Event> execute(GenericEvent payload) {
		List<Event> events = new ArrayList<>();
		
		IncidentTicketNotificationEventDto siaNotification = JsonUtils.readObjectFromJSON(payload.getPayloadJson(), IncidentTicketNotificationEventDto.class);
		
		//toto moze znamenat, ze sa o niektorych vypadkoch nedozvedia ani salesaci za danych zakanikov... je to ok?
		siaNotification.filterOutLAsNotInConfig(config);
		
		List<Event> customerEvents = createCustomersEvents(siaNotification);	
		List<Event> salesEvents = createSalesEvents(siaNotification);
		
		events.addAll(customerEvents);
		events.addAll(salesEvents);
		return events;
	}

	public List<Event> createCustomersEvents(
			IncidentTicketNotificationEventDto notifEvent
			) {
		List<Event> customerEvents = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> outageForCustomers = groupByCustomerPerson(notifEvent.getMessages());
		
		for (IncidentTicketNotificationContactDto customer: outageForCustomers.keySet()) {
			Event event  = createCustomerEvent(notifEvent.getOutageStart(), customer, outageForCustomers.get(customer));
			customerEvents.add(event);
		}
		return customerEvents;
	} 

	private Event createCustomerEvent(
			Date outageStart,
			IncidentTicketNotificationContactDto customer, 
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer) {
		
		CustEventStartEmailTemplate customerMessageContent = new CustEventStartEmailTemplate();
		
		customerMessageContent.setSubjectResourceKey("cust.start.subject");
		customerMessageContent.setTemplateFileName("customer-notification-outage-start.html");
		customerMessageContent.setRequiredLocales(Arrays.asList(new Locale("en"), new Locale("sk")));
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutagesForCustomer);
		String customerName = extractCustomerName(serviceOutagesForCustomer);
		customerMessageContent.setModel(new CustEventStartModel(outageStart, customerName, outageInfos));

		Set<EmailEndpoint> customerEmails = customer.asEmailEnpoints();
		
		Body eventBody = new Body();
		eventBody.setMessage(customerMessageContent);
		eventBody.getRecievingEndpoints().addAll(customerEmails);
		
		Event event = new Event();
		event.setBody(eventBody);
		
		return event;
	}	
	
	public List<Event> createSalesEvents(IncidentTicketNotificationEventDto notifEvent) {
		List<Event> salesEvents = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidentsForSellers = groupBySalesPerson(notifEvent.getMessages());
		
		for (IncidentTicketNotificationContactDto salesContact: incidentsForSellers.keySet()) {
			Event event  = createSalesEvent(notifEvent.getOutageStart(), salesContact, incidentsForSellers.get(salesContact));
			salesEvents.add(event);
		}
		return salesEvents;
	}
	
	private Event createSalesEvent(
			Date outageStart,
			IncidentTicketNotificationContactDto salesContact, 
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutages) {
		
		SalesEventStartEmailTemplate salesMessageContent = new SalesEventStartEmailTemplate();
		
		salesMessageContent.setSubjectResourceKey("sales.start.subject");
		salesMessageContent.setTemplateFileName("sales-notification-outage-start.html");
		salesMessageContent.setRequiredLocales(Arrays.asList(new Locale("sk")));
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutages);
		Map<String, List<ServiceOutageInfo>> outageInfosPerCustomer = outageInfos.stream().collect(Collectors.groupingBy(ServiceOutageInfo::getCustomerName));
		
		salesMessageContent.setModel(new SalesEventStartModel(outageStart, outageInfosPerCustomer));

		Set<EmailEndpoint> customerEmails = salesContact.asEmailEnpoints();
		
		Body eventBody = new Body();
		eventBody.setMessage(salesMessageContent);
		eventBody.getRecievingEndpoints().addAll(customerEmails);
		
		Event event = new Event();
		event.setBody(eventBody);
		
		return event;
	}	

	
	private Set<IncidentTicketNotificationContactDto> extractDistinctSalesContacts(List<IncidentTicketServiceOutageForCustomerDto> messages) {
		return messages.stream()
					.map(outage -> outage.getSellerContacts())
					.flatMap(contacts -> contacts.stream())
					.collect(Collectors.toSet());
	}
	
	private Set<IncidentTicketNotificationContactDto> extractDistinctCustomerContacts(List<IncidentTicketServiceOutageForCustomerDto> messages) {
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
	
	private String extractCustomerName(List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer) {
		return serviceOutagesForCustomer.iterator().next().getCustomerName();
	}
	
	private Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> groupBySalesPerson(
				List<IncidentTicketServiceOutageForCustomerDto> messages
			) {
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidentsForSellers = new HashMap<>();
		
		Set<IncidentTicketNotificationContactDto> salesContacts = extractDistinctSalesContacts(messages);
		
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
	private Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> groupByCustomerPerson(
			List<IncidentTicketServiceOutageForCustomerDto> messages
			) {
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidetnsForCustomerContact = new HashMap<>();
		
		Set<IncidentTicketNotificationContactDto> customerContacts = extractDistinctCustomerContacts(messages);
		
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
