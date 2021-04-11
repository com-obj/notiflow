package com.obj.nc.osk.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.obj.nc.osk.domain.IncidentTicketServiceOutageForCustomerDto.CustomerSegment;
import com.obj.nc.osk.functions.processors.eventConverter.config.NotifEventConverterConfigProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Don't change, this is part of communication protocol with SIA application
 * @author ja
 *
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncidentTicketOutageStartEventDto extends SiaOutageEvent {
	
	String name;
	String description;
	Date outageStart;
	Date outageEnd;
	@Singular
	List<IncidentTicketServiceOutageForCustomerDto> messages;

	
	public void filterOutLAsNotInConfig(NotifEventConverterConfigProperties config) {
		messages = 
			getMessages().stream()
				.filter(msg -> 
							msg.getCustomerSegment() == CustomerSegment.SME
							||
							(msg.getCustomerSegment() == CustomerSegment.LA 
								&& 
							config.getB2bLoginOfLACustumersToBeNotified().contains(msg.getB2bLogin()))
				)
			.collect(Collectors.toList());
	}
	
	public List<IncidentTicketServiceOutageForCustomerDto> getMessages() {
		if (this.messages == null) {
			this.messages = new ArrayList<IncidentTicketServiceOutageForCustomerDto>();
		}

		return messages;
	}
}
