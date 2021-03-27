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
import com.obj.nc.domain.message.BaseEmailFromTemplate;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.osk.dto.IncidentTicketNotificationContactDto;
import com.obj.nc.osk.dto.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.dto.IncidentTicketServiceOutageForCustomerDto;
import com.obj.nc.osk.functions.config.NotifEventConverterConfig;
import com.obj.nc.osk.functions.content.CustEventEmailTemplate;
import com.obj.nc.osk.functions.content.SalesAgentsEventEmailTemplate;
import com.obj.nc.osk.functions.content.SalesEventEmailTemplate;
import com.obj.nc.osk.functions.model.CustEventModel;
import com.obj.nc.osk.functions.model.CustomerInfo;
import com.obj.nc.osk.functions.model.SalesAgentEventModel;
import com.obj.nc.osk.functions.model.SalesEventModel;
import com.obj.nc.osk.functions.model.ServiceOutageInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@Data
@EqualsAndHashCode(callSuper=false)
@RequiredArgsConstructor
public abstract class BaseOutageEventConverter extends ProcessorFunctionAdapter<GenericEvent, List<Event>> {
	
	@NonNull
	private NotifEventConverterConfig config;
	
	protected String customerEmailSubjectKey;
	protected String customerEmailTemplateName;

	protected String salesEmailSubjectKey;
	protected String salesEmailTemplateName;
	
	protected String salesAgentEmailSubjectKey;
	protected String salesAgentEmailTemplateName;

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
		
		IncidentTicketOutageStartEventDto siaNotification = findIncidentTicketStartEvent(payload);
		
		//toto moze znamenat, ze sa o niektorych vypadkoch nedozvedia ani salesaci za danych zakanikov... je to ok?
		siaNotification.filterOutLAsNotInConfig(config);
		
		List<Event> customerEvents = createCustomersEvents(siaNotification);	
		List<Event> salesEvents = createSalesEvents(siaNotification);
		List<Event> salesAgentEvents = createSalesAgentsEvents(siaNotification);

		events.addAll(customerEvents);
		events.addAll(salesEvents);
		events.addAll(salesAgentEvents);
		
		events.forEach(e-> e.getHeader().addEventId(payload.getId()));
		
		return events;
	}

	protected abstract IncidentTicketOutageStartEventDto findIncidentTicketStartEvent(GenericEvent payload);
	
	protected CustEventEmailTemplate createCustomerEmailContent(
			Date outageStart,Date outageEnd,
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer
			) {
		CustEventEmailTemplate customerMessageContent = new CustEventEmailTemplate();
		
		customerMessageContent.setSubjectResourceKey(customerEmailSubjectKey);
		customerMessageContent.setTemplateFileName(customerEmailTemplateName);
		customerMessageContent.setRequiredLocales(Arrays.asList(new Locale("en"), new Locale("sk")));
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutagesForCustomer);
		String customerName = extractCustomerName(serviceOutagesForCustomer);
		customerMessageContent.setModel(new CustEventModel(outageStart, outageEnd, customerName, outageInfos));
		return customerMessageContent;
	}	
	
	
	protected SalesEventEmailTemplate createSalesEmailContent(
			Date outageStart, Date outageEnd,
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutages
			) {
		SalesEventEmailTemplate salesMessageContent = new SalesEventEmailTemplate();
		
		salesMessageContent.setSubjectResourceKey(salesEmailSubjectKey);
		salesMessageContent.setTemplateFileName(salesEmailTemplateName);
		salesMessageContent.setRequiredLocales(Arrays.asList(new Locale("sk")));
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutages);
		Map<CustomerInfo, List<ServiceOutageInfo>> outageInfosPerCustomer = outageInfos
				.stream()
				.collect(Collectors.groupingBy(ServiceOutageInfo::getCustomer));
		
		salesMessageContent.setModel(new SalesEventModel(outageStart, outageEnd, outageInfosPerCustomer));
		return salesMessageContent;
	}		
	protected SalesAgentsEventEmailTemplate createSalesAgentsEmailContent(
			Date outageStart, Date outageEnd,
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutages) {
		SalesAgentsEventEmailTemplate salesAgentMessageContent = new SalesAgentsEventEmailTemplate();
		
		salesAgentMessageContent.setSubjectResourceKey(salesAgentEmailSubjectKey);
		salesAgentMessageContent.setTemplateFileName(salesAgentEmailTemplateName);
		salesAgentMessageContent.setRequiredLocales(Arrays.asList(new Locale("sk")));
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutages);
		
		salesAgentMessageContent.setModel(new SalesAgentEventModel(outageStart, outageEnd, outageInfos));
		return salesAgentMessageContent;
	}	

	protected List<Event> createCustomersEvents(
			IncidentTicketOutageStartEventDto notifEvent
			) {
		List<Event> customerEvents = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> outageForCustomers = groupByCustomerPerson(notifEvent.getMessages());
		
		for (IncidentTicketNotificationContactDto customer: outageForCustomers.keySet()) {
			
			CustEventEmailTemplate customerMessageContent = createCustomerEmailContent(
					notifEvent.getOutageStart(), 
					notifEvent.getOutageEnd(),
					outageForCustomers.get(customer));

			
			Event event  = createNotificationIntent(customerMessageContent, customer.asEmailEnpoints());
			customerEvents.add(event);
		}
		return customerEvents;
	} 
	
	private Event createNotificationIntent(
			BaseEmailFromTemplate<?> messageContent,
			Set<EmailEndpoint> emails) {
		
		Body eventBody = new Body();
		eventBody.setMessage(messageContent);
		eventBody.getRecievingEndpoints().addAll(emails);
		
		Event event = new Event();
		event.setBody(eventBody);
		
		return event;
	}

	
	protected List<Event> createSalesEvents(IncidentTicketOutageStartEventDto notifEvent) {
		List<Event> salesEvents = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidentsForSellers = groupBySalesPerson(notifEvent.getMessages());
		
		for (IncidentTicketNotificationContactDto salesContact: incidentsForSellers.keySet()) {
			
			SalesEventEmailTemplate salesMessageContent = createSalesEmailContent(
					notifEvent.getOutageStart(), 
					notifEvent.getOutageEnd(),
					incidentsForSellers.get(salesContact));
			
			Event event  = createNotificationIntent(
					salesMessageContent,
					salesContact.asEmailEnpoints());
			
			salesEvents.add(event);
		}
		return salesEvents;
	}
	
	protected List<Event> createSalesAgentsEvents(IncidentTicketOutageStartEventDto notifEvent) {
		List<Event> salesEvents = new ArrayList<>();
		
		Set<EmailEndpoint> agentEmails = config.getCsAgentsToNotifyEmail().stream()
				.map(email -> new EmailEndpoint(email))
				.collect(Collectors.toSet());

			
		SalesAgentsEventEmailTemplate salesMessageContent = createSalesAgentsEmailContent(
				notifEvent.getOutageStart(), 
				notifEvent.getOutageEnd(),
				notifEvent.getMessages());
			
		Event event  = createNotificationIntent(
				salesMessageContent, 
				agentEmails);
		
		salesEvents.add(event);

		return salesEvents;
	}
	
	protected Set<IncidentTicketNotificationContactDto> extractDistinctSalesContacts(List<IncidentTicketServiceOutageForCustomerDto> messages) {
		return messages.stream()
					.map(outage -> outage.getSellerContacts())
					.flatMap(contacts -> contacts.stream())
					.collect(Collectors.toSet());
	}
	
	protected Set<IncidentTicketNotificationContactDto> extractDistinctCustomerContacts(List<IncidentTicketServiceOutageForCustomerDto> messages) {
		return messages.stream()
					.map(outage -> outage.getCustomerContacts())
					.flatMap(contacts -> contacts.stream())
					.collect(Collectors.toSet());
	}

	protected List<ServiceOutageInfo> convertToServiceOutages(List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer) {
		List<ServiceOutageInfo> outageInfos = new ArrayList<>();
		
		for (IncidentTicketServiceOutageForCustomerDto serviceOutage : serviceOutagesForCustomer) {
			ServiceOutageInfo outage = ServiceOutageInfo.from(serviceOutage);
			
			outageInfos.add(outage);
		}
		
		return outageInfos;
	}
	
	protected String extractCustomerName(List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer) {
		return serviceOutagesForCustomer.iterator().next().getCustomerName();
	}
	
	protected Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> groupBySalesPerson(
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
	protected Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> groupByCustomerPerson(
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
