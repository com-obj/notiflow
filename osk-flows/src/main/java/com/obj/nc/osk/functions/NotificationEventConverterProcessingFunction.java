package com.obj.nc.osk.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.Body;
import com.obj.nc.domain.endpoints.EmailEndpoint;
import com.obj.nc.domain.endpoints.Person;
import com.obj.nc.domain.endpoints.Recipient;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.domain.message.MessageContent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.osk.dto.IncidentTicketNotificationContactDto;
import com.obj.nc.osk.dto.IncidentTicketNotificationEventDto;
import com.obj.nc.osk.dto.IncidentTicketServiceOutageForCustomerDto;
import com.obj.nc.osk.dto.ServiceOutageInfo;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class NotificationEventConverterProcessingFunction extends ProcessorFunctionAdapter<GenericEvent, List<Event>> {

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
		
		List<Event> customerEvents = createCustomersEvents(siaNotification);
		
		events.addAll(customerEvents);
		return events;
	}

	private List<Event> createCustomersEvents(IncidentTicketNotificationEventDto siaNotification) {
		List<Event> customerEvents = new ArrayList<>();
		Map<Long, List<IncidentTicketServiceOutageForCustomerDto>> outageForCustomers = groupByBillingAccount(siaNotification.getMessages());
		for (Long b2bLogin: outageForCustomers.keySet()) {
			Body eventBody  = createCustomerEvent(siaNotification, outageForCustomers.get(b2bLogin));
			
			Event event = new Event();
			
			event.setBody(eventBody);
			customerEvents.add(event);
		}
		return customerEvents;
	}
	
	private Body createCustomerEvent(IncidentTicketNotificationEventDto siaNotification, List<IncidentTicketServiceOutageForCustomerDto> serviceOutages) {
		MessageContent customerMessageContent = new MessageContent();
		customerMessageContent.putAttributeValue("outageStart", siaNotification.getOutageStart());
		customerMessageContent.setSubject("Vase sluzby mozu byt nedostupne");
		customerMessageContent.setText("Vážený zákazník, o xx:xx hod. sme zaznamenali výpadok..."); 
		
		List<ServiceOutageInfo> outageInfos = convertToServiceOutages(serviceOutages);
		customerMessageContent.putAttributeValue("outageInfos", outageInfos);

		List<EmailEndpoint> customerEmails = extractCumtomerEmails(serviceOutages);
		
		Body eventBody = new Body();
		eventBody.getMessage().setContent(customerMessageContent);
		eventBody.getRecievingEndpoints().addAll(customerEmails);
		
		return eventBody;
	}

	private List<EmailEndpoint> extractCumtomerEmails(List<IncidentTicketServiceOutageForCustomerDto> serviceOutages) {
		List<EmailEndpoint> customerEmails = new ArrayList<>();
		IncidentTicketServiceOutageForCustomerDto anyOutage = serviceOutages.iterator().next();
		for (IncidentTicketNotificationContactDto customerContant: anyOutage.getCustomerContacts()) {
			Person customer = new Person(customerContant.getName());
			
			for (String email: customerContant.getEmails()) {
				EmailEndpoint customerEmail = EmailEndpoint.createForPerson(customer,email);
				customerEmails.add(customerEmail);
			}
		}
		return customerEmails;
	}

	private List<ServiceOutageInfo> convertToServiceOutages(
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutages) {
		List<ServiceOutageInfo> outageInfos = new ArrayList<>();
		for (IncidentTicketServiceOutageForCustomerDto serviceOutage : serviceOutages) {
			ServiceOutageInfo outage = new ServiceOutageInfo();
			outage.setB2bLogin(serviceOutage.getB2bLogin());
			outage.setProductName(serviceOutage.getService());
			outage.setSn(serviceOutage.getSn());
			outage.setInstallationAddress(serviceOutage.getInstallationAddress());
			
			outageInfos.add(outage);
		}
		return outageInfos;
	}
	
	private Map<Long, List<IncidentTicketServiceOutageForCustomerDto>> groupByBillingAccount(
			List<IncidentTicketServiceOutageForCustomerDto> serviceOutages) {
		
		return serviceOutages
				.stream()
				.collect(Collectors.groupingBy(
						IncidentTicketServiceOutageForCustomerDto::getBillingAccountId
						)
				);
	}

}
