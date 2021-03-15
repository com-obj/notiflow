package com.obj.nc.osk.dto;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.obj.nc.osk.config.StaticRoutingOptions;
import com.obj.nc.osk.dto.IncidentTicketServiceOutageForCustomerDto.CustomerSegment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class IncidentTicketNotificationEventDto {
	Long id;
	String name;
	String description;
	Date outageStart;
	Date outageEnd;
	List<IncidentTicketServiceOutageForCustomerDto> messages;
	
	public void filterOutLAsNotInConfig(StaticRoutingOptions config) {
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
