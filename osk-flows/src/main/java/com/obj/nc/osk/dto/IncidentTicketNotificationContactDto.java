package com.obj.nc.osk.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Don't change, this is part of communication protocol with SIA application
 * @author ja
 *
 */
@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class IncidentTicketNotificationContactDto {
	Long id;
	String name;
	List<String> emails;
	List<String> phones;
}
