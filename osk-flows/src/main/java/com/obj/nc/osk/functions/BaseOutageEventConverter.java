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
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.RecievingEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.osk.domain.incidentTicket.IncidentTicketNotificationContactDto;
import com.obj.nc.osk.domain.incidentTicket.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.domain.incidentTicket.IncidentTicketServiceOutageForCustomerDto;
import com.obj.nc.osk.functions.config.NotifEventConverterConfig;
import com.obj.nc.osk.functions.content.CustEmailTemplate;
import com.obj.nc.osk.functions.content.CustSmsTemplate;
import com.obj.nc.osk.functions.content.SalesAgentsEmailTemplate;
import com.obj.nc.osk.functions.content.SalesEmailTemplate;
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
public abstract class BaseOutageEventConverter extends ProcessorFunctionAdapter<GenericEvent, List<NotificationIntent>> {
	
	@NonNull
	private NotifEventConverterConfig config;
	
	protected String customerEmailSubjectKey;
	protected String customerEmailTemplateName;
	
	protected String customerSmsTemplateName;

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
	protected List<NotificationIntent> execute(GenericEvent payload) {
		List<NotificationIntent> notificationIntents = new ArrayList<>();
		
		IncidentTicketOutageStartEventDto siaNotification = findIncidentTicketStartEvent(payload);
		
		//toto moze znamenat, ze sa o niektorych vypadkoch nedozvedia ani salesaci za danych zakanikov... je to ok?
		siaNotification.filterOutLAsNotInConfig(config);
		
		List<NotificationIntent> customerNotificationIntents = createCustomersNotificationIntents(siaNotification);	
		List<NotificationIntent> salesNotificationIntents = createSalesNotificationIntents(siaNotification);
		List<NotificationIntent> salesAgentNotificationIntents = createSalesAgentsNotificationIntents(siaNotification);

		notificationIntents.addAll(customerNotificationIntents);
		notificationIntents.addAll(salesNotificationIntents);
		notificationIntents.addAll(salesAgentNotificationIntents);
		
		notificationIntents.forEach(e-> e.getHeader().addEventId(payload.getId()));
		
		return notificationIntents;
	}

	protected abstract IncidentTicketOutageStartEventDto findIncidentTicketStartEvent(GenericEvent payload);
	
	protected CustEmailTemplate createCustomerEmailContent(
			Date outageStart,Date outageEnd,
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer
			) {
		CustEmailTemplate customerMessageContent = new CustEmailTemplate();
		
		customerMessageContent.setSubjectResourceKey(customerEmailSubjectKey);
		customerMessageContent.setTemplateFileName(customerEmailTemplateName);
		customerMessageContent.setRequiredLocales(Arrays.asList(new Locale("en"), new Locale("sk")));
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutagesForCustomer);
		String customerName = extractCustomerName(serviceOutagesForCustomer);
		customerMessageContent.setModel(new CustEventModel(outageStart, outageEnd, customerName, outageInfos));
		return customerMessageContent;
	}	
	
	protected CustSmsTemplate createCustomerSmsContent(
			Date outageStart,Date outageEnd,
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer
			) {
		CustSmsTemplate customerMessageContent = new CustSmsTemplate();
		
		customerMessageContent.setTemplateFileName(customerSmsTemplateName);
		customerMessageContent.setRequiredLocales(Arrays.asList(new Locale("sk")));
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutagesForCustomer);
		String customerName = extractCustomerName(serviceOutagesForCustomer);
		customerMessageContent.setModel(new CustEventModel(outageStart, outageEnd, customerName, outageInfos));
		return customerMessageContent;
	}
	
	
	protected SalesEmailTemplate createSalesEmailContent(
			Date outageStart, Date outageEnd,
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutages
			) {
		SalesEmailTemplate salesMessageContent = new SalesEmailTemplate();
		
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
	protected SalesAgentsEmailTemplate createSalesAgentsEmailContent(
			Date outageStart, Date outageEnd,
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutages) {
		SalesAgentsEmailTemplate salesAgentMessageContent = new SalesAgentsEmailTemplate();
		
		salesAgentMessageContent.setSubjectResourceKey(salesAgentEmailSubjectKey);
		salesAgentMessageContent.setTemplateFileName(salesAgentEmailTemplateName);
		salesAgentMessageContent.setRequiredLocales(Arrays.asList(new Locale("sk")));
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutages);
		
		salesAgentMessageContent.setModel(new SalesAgentEventModel(outageStart, outageEnd, outageInfos));
		return salesAgentMessageContent;
	}	

	protected List<NotificationIntent> createCustomersNotificationIntents(
			IncidentTicketOutageStartEventDto notifEvent
			) {
		List<NotificationIntent> customerNotificationIntents = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> outageForCustomers = groupByCustomerPerson(notifEvent.getMessages());
		
		for (IncidentTicketNotificationContactDto customer: outageForCustomers.keySet()) {
			
			if (customer.asEmailEnpoints().size() > 0) {
				CustEmailTemplate customerEmailContent = createCustomerEmailContent(
						notifEvent.getOutageStart(), 
						notifEvent.getOutageEnd(),
						outageForCustomers.get(customer));
	
				NotificationIntent notificationIntent  = createNotificationIntent(customerEmailContent, customer.asEmailEnpoints());
				customerNotificationIntents.add(notificationIntent);
			}
			
			if (customer.asSmsEnpoints().size() > 0) {
				CustSmsTemplate customerSmsContent = createCustomerSmsContent(
						notifEvent.getOutageStart(), 
						notifEvent.getOutageEnd(),
						outageForCustomers.get(customer));
	
				NotificationIntent notificationIntent  = createNotificationIntent(customerSmsContent, customer.asSmsEnpoints());
				customerNotificationIntents.add(notificationIntent);
			}
		}
		return customerNotificationIntents;
	} 
	
	private NotificationIntent createNotificationIntent(
			TemplateWithModelContent<?> messageContent,
			Set<? extends RecievingEndpoint> endpoints) {
		
		Body eventBody = new Body();
		eventBody.setMessage(messageContent);
		eventBody.getRecievingEndpoints().addAll(endpoints);
		
		NotificationIntent notificationIntent = new NotificationIntent();
		notificationIntent.setBody(eventBody);
		
		return notificationIntent;
	}

	
	protected List<NotificationIntent> createSalesNotificationIntents(IncidentTicketOutageStartEventDto notifEvent) {
		List<NotificationIntent> salesNotificationIntents = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidentsForSellers = groupBySalesPerson(notifEvent.getMessages());
		
		for (IncidentTicketNotificationContactDto salesContact: incidentsForSellers.keySet()) {
			
			SalesEmailTemplate salesMessageContent = createSalesEmailContent(
					notifEvent.getOutageStart(), 
					notifEvent.getOutageEnd(),
					incidentsForSellers.get(salesContact));
			
			NotificationIntent notificationIntent  = createNotificationIntent(
					salesMessageContent,
					salesContact.asEmailEnpoints());
			
			salesNotificationIntents.add(notificationIntent);
		}
		return salesNotificationIntents;
	}
	
	protected List<NotificationIntent> createSalesAgentsNotificationIntents(IncidentTicketOutageStartEventDto notifEvent) {
		List<NotificationIntent> salesNotificationIntents = new ArrayList<>();
		
		Set<EmailEndpoint> agentEmails = config.getCsAgentsToNotifyEmail().stream()
				.map(email -> new EmailEndpoint(email))
				.collect(Collectors.toSet());

			
		SalesAgentsEmailTemplate salesMessageContent = createSalesAgentsEmailContent(
				notifEvent.getOutageStart(), 
				notifEvent.getOutageEnd(),
				notifEvent.getMessages());
			
		NotificationIntent notificationIntent  = createNotificationIntent(
				salesMessageContent, 
				agentEmails);
		
		salesNotificationIntents.add(notificationIntent);

		return salesNotificationIntents;
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
