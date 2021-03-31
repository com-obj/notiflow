package com.obj.nc.osk.functions;

import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.domain.incidentTicket.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.functions.config.NotifEventConverterConfig;
import com.obj.nc.utils.JsonUtils;

@Component
public class StartOutageEventConverter extends BaseOutageEventConverter {

	public StartOutageEventConverter(NotifEventConverterConfig config) {
		super(config);
		
		customerEmailSubjectKey = "cust.start.subject";
		customerEmailTemplateName = "customer-notification-outage-start.html";
		
		customerSmsTemplateName = "customer-notification-outage-start.txt";

		salesEmailSubjectKey = "sales.start.subject";
		salesEmailTemplateName = "sales-notification-outage-start.html";
		
		salesAgentEmailSubjectKey = "salesAgent.start.subject";
		salesAgentEmailTemplateName = "agent-notification-outage-start.html";
	}

	protected IncidentTicketOutageStartEventDto findIncidentTicketStartEvent(GenericEvent payload) {
		return JsonUtils.readObjectFromJSON(payload.getPayloadJson(), IncidentTicketOutageStartEventDto.class);
	}



}
