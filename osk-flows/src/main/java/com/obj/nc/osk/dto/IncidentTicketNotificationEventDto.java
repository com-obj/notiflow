package com.obj.nc.osk.dto;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.osk.dto.IncidentTicketServiceOutageForCustomerDto.CustomerSegment;
import com.obj.nc.osk.functions.NotifEventConverterConfig;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@JsonTypeName("OUTAGE_START")
public class IncidentTicketNotificationEventDto extends SiaOutageEvent {
	
	String name;
	String description;
	Date outageStart;
	Date outageEnd;
	List<IncidentTicketServiceOutageForCustomerDto> messages;

	
	public void filterOutLAsNotInConfig(NotifEventConverterConfig config) {
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
}
