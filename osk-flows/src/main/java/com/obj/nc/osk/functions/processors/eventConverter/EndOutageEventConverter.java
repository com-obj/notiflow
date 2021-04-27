package com.obj.nc.osk.functions.processors.eventConverter;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.osk.domain.IncidentTicketOutageEndEventDto;
import com.obj.nc.osk.domain.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.functions.processors.eventConverter.config.NotifEventConverterConfigProperties;
import com.obj.nc.osk.repositories.ExtendedGenericEventRepository;
import com.obj.nc.utils.JsonUtils;

@DocumentProcessingInfo("EndOutageEventConverter")
public class EndOutageEventConverter extends BaseOutageEventConverter {
	
	private ExtendedGenericEventRepository customEventRepo;

	public EndOutageEventConverter(
			@Autowired NotifEventConverterConfigProperties config,
			@Autowired ExtendedGenericEventRepository eventRepo) {
		super(config);
		
		this.customEventRepo = eventRepo;
		
		customerEmailSubjectKey = "cust.end.subject";
		customerEmailTemplateName = "customer-notification-outage-end.html";
		
		customerSmsTemplateName = "customer-notification-outage-end.txt";

		salesEmailSubjectKey = "sales.end.subject";
		salesEmailTemplateName = "sales-notification-outage-end.html";
		
		salesAgentEmailSubjectKey = "salesAgent.end.subject";
		salesAgentEmailTemplateName = "agent-notification-outage-end.html";
	}

	protected IncidentTicketOutageStartEventDto findIncidentTicketStartEvent(GenericEvent endEvent) {
		IncidentTicketOutageEndEventDto endEventPayload = JsonUtils.readObjectFromJSON(endEvent.getPayloadJson(), IncidentTicketOutageEndEventDto.class);
		
		Optional<GenericEvent> startEvent = customEventRepo.findStartEventByTicketId(endEventPayload.getId());

		if (!startEvent.isPresent()) {
			throw new PayloadValidationException("Didn't find matching start event with externalId="+ endEventPayload.getId()+" for outage end event: " + endEventPayload);
		}
		
		IncidentTicketOutageStartEventDto startEventPayload = JsonUtils.readObjectFromJSON(startEvent.get().getPayloadJson(), IncidentTicketOutageStartEventDto.class);
		startEventPayload.setOutageEnd(endEventPayload.getOutageEnd());
		
		return startEventPayload;
	}



}
