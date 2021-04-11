package com.obj.nc.osk.functions.processors.eventConverter;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.osk.domain.IncidentTicketOutageStartEventDto;
import com.obj.nc.osk.functions.processors.eventConverter.config.NotifEventConverterConfigProperties;
import com.obj.nc.utils.JsonUtils;

@DocumentProcessingInfo("StartOutageEventConverter")
public class StartOutageEventConverter extends BaseOutageEventConverter {

	public StartOutageEventConverter(NotifEventConverterConfigProperties config) {
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
