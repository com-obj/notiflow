package com.obj.nc.osk.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.obj.nc.aspects.DocumentProcessingInfo;
import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.event.GenericEvent;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.processors.ProcessorFunctionAdapter;
import com.obj.nc.osk.config.StaticRoutingOptions;
import com.obj.nc.osk.dto.IncidentTicketNotificationEventDto;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class NotificationEventConverterProcessingFunction extends ProcessorFunctionAdapter<GenericEvent, List<Event>> {
	
	private StaticRoutingOptions config;
	
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
		
		List<Event> customerEvents = siaNotification.createCustomersEvents();	
		List<Event> salesEvents = siaNotification.createSalesEvents();
		
		events.addAll(customerEvents);
		events.addAll(salesEvents);
		return events;
	}
	



	


}
