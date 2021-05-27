package com.obj.nc.osk.functions.processors.eventConverter;

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

import com.google.common.collect.Lists;
import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.SmsEndpoint;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.EmailWithTemplatedContent;
import com.obj.nc.domain.message.Message;
import com.obj.nc.domain.message.SmsWithTemplatedContent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.osk.domain.IncidentTicketNotificationContactDto;
import com.obj.nc.osk.domain.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.domain.IncidentTicketServiceOutageForCustomerDto;
import com.obj.nc.osk.functions.content.CustEmailTemplate;
import com.obj.nc.osk.functions.content.CustSmsTemplate;
import com.obj.nc.osk.functions.content.SalesAgentsEmailTemplate;
import com.obj.nc.osk.functions.content.SalesEmailTemplate;
import com.obj.nc.osk.functions.model.CustEventModel;
import com.obj.nc.osk.functions.model.CustomerInfo;
import com.obj.nc.osk.functions.model.SalesAgentEventModel;
import com.obj.nc.osk.functions.model.SalesEventModel;
import com.obj.nc.osk.functions.model.ServiceOutageInfo;
import com.obj.nc.osk.functions.processors.eventConverter.config.NotifEventConverterConfigProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@Data
@EqualsAndHashCode(callSuper=false)
@RequiredArgsConstructor
@DocumentProcessingInfo
public abstract class BaseOutageEventConverter extends ProcessorFunctionAdapter<GenericEvent, List<Message<?>>> {
	
	@NonNull
	private NotifEventConverterConfigProperties config;
	
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
			return Optional.of(new PayloadValidationException("FailedPaylod must not be null"));
		}
		
		if (payload.getPayloadJson()==null) {
			return Optional.of(new PayloadValidationException("FailedPaylod doesn't contain original message"));
		}

		return Optional.empty();
	}

	@Override
	protected List<Message<?>> execute(GenericEvent payload) {
		List<Message<?>> notifications = new ArrayList<>();
		
		IncidentTicketOutageStartEventDto siaNotification = findIncidentTicketStartEvent(payload);
		
		//toto moze znamenat, ze sa o niektorych vypadkoch nedozvedia ani salesaci za danych zakanikov... je to ok?
		siaNotification.filterOutLAsNotInConfig(config);
		
		List<Message<?>> customerNotification = createCustomersNotificationIntents(siaNotification);	
		List<Message<?>> salesNotification = createSalesNotificationIntents(siaNotification);
		List<Message<?>> salesAgentNotification = createSalesAgentsNotificationIntents(siaNotification);

		notifications.addAll(customerNotification);
		notifications.addAll(salesNotification);
		notifications.addAll(salesAgentNotification);
		
		return notifications;
	}

	protected abstract IncidentTicketOutageStartEventDto findIncidentTicketStartEvent(GenericEvent payload);
	
	protected CustEmailTemplate createCustomerEmailContent(
			Date outageStart,Date outageEnd,
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutagesForCustomer
			) {
		CustEmailTemplate customerMessageContent = new CustEmailTemplate();
		
		customerMessageContent.setSubjectResourceKey(customerEmailSubjectKey);
		customerMessageContent.setTemplateFileName(customerEmailTemplateName);
		customerMessageContent.setRequiredLocales(Arrays.asList(new Locale("sk"), new Locale("en")));
		
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
		
		int customerCount = outageInfosPerCustomer.keySet().size();
		salesMessageContent.setModel(new SalesEventModel(outageStart, outageEnd, outageInfosPerCustomer, customerCount));
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
		Map<CustomerInfo, List<ServiceOutageInfo>> outageInfosPerCustomer = outageInfos
				.stream()
				.collect(Collectors.groupingBy(ServiceOutageInfo::getCustomer));
		
		int customerCount = outageInfosPerCustomer.keySet().size();
		salesAgentMessageContent.setModel(new SalesAgentEventModel(outageStart, outageEnd, outageInfos, customerCount));
		return salesAgentMessageContent;
	}	

	protected List<Message<?>> createCustomersNotificationIntents(
			IncidentTicketOutageStartEventDto notifEvent
			) {
		List<Message<?>> customeNotifications = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> outageForCustomers = groupByCustomerPerson(notifEvent.getMessages());
		
		for (IncidentTicketNotificationContactDto customer: outageForCustomers.keySet()) {
			
			if (customer.asEmailEnpoints().size() > 0) {
				CustEmailTemplate customerEmailContent = createCustomerEmailContent(
						notifEvent.getOutageStart(), 
						notifEvent.getOutageEnd(),
						outageForCustomers.get(customer));
	
				Message<?> notification  = createEmailNotification(customerEmailContent, customer.asEmailEnpoints());
				customeNotifications.add(notification);
			}
			
			if (customer.asSmsEnpoints().size() > 0) {
				CustSmsTemplate customerSmsContent = createCustomerSmsContent(
						notifEvent.getOutageStart(), 
						notifEvent.getOutageEnd(),
						outageForCustomers.get(customer));
	
				Message<?> notificationIntent  = createSmsNotification(customerSmsContent, customer.asSmsEnpoints());
				customeNotifications.add(notificationIntent);
			}
		}
		return customeNotifications;
	} 
	
//	private NotificationIntent<?> createNotification(
//			TemplateWithModelContent<?> messageContent,
//			Set<? extends RecievingEndpoint> endpoints) {
//				
//		NotificationIntent<TemplateWithModelContent<?>> notificationIntent = new NotificationIntent<
//				>();
//		notificationIntent.setBody(messageContent);
//		
//		List<RecievingEndpoint> typedEndpoints = Lists.newArrayList(
//				   Iterables.filter(endpoints, RecievingEndpoint.class));
//		notificationIntent.setRecievingEndpoints(typedEndpoints);
//		
//		return notificationIntent;
//	}
	
	private <CONTENT_TYPE extends TemplateWithModelEmailContent<?>> EmailWithTemplatedContent<CONTENT_TYPE> createEmailNotification(
			CONTENT_TYPE messageContent,
			Set<EmailEndpoint> endpoints) {
				
		EmailWithTemplatedContent<CONTENT_TYPE> notification = new EmailWithTemplatedContent<>();
		notification.setBody(messageContent);
		
		notification.setRecievingEndpoints(Lists.newArrayList(endpoints));
		
		return notification;
	}	
	
	private SmsWithTemplatedContent<CustSmsTemplate> createSmsNotification(
			CustSmsTemplate messageContent,
			Set<SmsEndpoint> endpoints) {
				
		SmsWithTemplatedContent<CustSmsTemplate> notification = new SmsWithTemplatedContent<>();
		notification.setBody(messageContent);
		
		notification.setRecievingEndpoints(Lists.newArrayList(endpoints));
		
		return notification;
	}	

	
	protected List<Message<?>> createSalesNotificationIntents(IncidentTicketOutageStartEventDto notifEvent) {
		List<Message<?>> salesNotification = new ArrayList<>();
		
		Map<IncidentTicketNotificationContactDto, List<IncidentTicketServiceOutageForCustomerDto>> incidentsForSellers = groupBySalesPerson(notifEvent.getMessages());
		
		for (IncidentTicketNotificationContactDto salesContact: incidentsForSellers.keySet()) {
			
			SalesEmailTemplate salesMessageContent = createSalesEmailContent(
					notifEvent.getOutageStart(), 
					notifEvent.getOutageEnd(),
					incidentsForSellers.get(salesContact));
			
			Message<?> notification = createEmailNotification(
					salesMessageContent,
					salesContact.asEmailEnpoints());
			
			salesNotification.add(notification);
		}
		return salesNotification;
	}
	
	protected List<Message<?>> createSalesAgentsNotificationIntents(IncidentTicketOutageStartEventDto notifEvent) {
		List<Message<?>> salesNotification = new ArrayList<>();
		
		Set<EmailEndpoint> agentEmails = config.getCsAgentsToNotifyEmail().stream()
				.map(email -> new EmailEndpoint(email))
				.collect(Collectors.toSet());

			
		SalesAgentsEmailTemplate salesMessageContent = createSalesAgentsEmailContent(
				notifEvent.getOutageStart(), 
				notifEvent.getOutageEnd(),
				notifEvent.getMessages());
			
		Message<?> notification = createEmailNotification(
				salesMessageContent, 
				agentEmails);
		
		salesNotification.add(notification);

		return salesNotification;
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
