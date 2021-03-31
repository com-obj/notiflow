package com.obj.nc.osk.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
@JsonTypeName("OUTAGE_END")
public class IncidentTicketOutageEndEventDto extends SiaOutageEvent {
	
	Date outageEnd;

}
