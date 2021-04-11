package com.obj.nc.osk.domain;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

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
public class IncidentTicketOutageEndEventDto extends SiaOutageEvent {
	
	Date outageEnd;

}
