package com.obj.nc.osk.domain.incidentTicket;

import java.util.ArrayList;
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
@NoArgsConstructor
public class IncidentTicketServiceOutageForCustomerDto {
	public enum CustomerSegment {
		SME, LA
	}

	CustomerSegment customerSegment;

	Long resourceId;
	String resource;

	Long billingAccountId;
	String billingAccount;

	Long subscriptionId;
	String subscription;

	Long serviceId;
	String service;

	String b2bLogin;
	String customerName;
	String customerAddress;
	
	String sn;
	String installationAddress;

	List<IncidentTicketNotificationContactDto> customerContacts = new ArrayList<>();
	List<IncidentTicketNotificationContactDto> sellerContacts = new ArrayList<>();

}