package com.obj.nc.osk.dto;

import java.util.Date;
import java.util.List;

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

}
