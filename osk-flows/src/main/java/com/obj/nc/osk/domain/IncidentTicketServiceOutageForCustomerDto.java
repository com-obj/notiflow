package com.obj.nc.osk.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 * Don't change, this is part of communication protocol with SIA application
 * @author ja
 *
 */
@Data
@Builder
@AllArgsConstructor
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

	@Singular
	List<IncidentTicketNotificationContactDto> customerContacts;
	@Singular
	List<IncidentTicketNotificationContactDto> sellerContacts;
	
	public List<IncidentTicketNotificationContactDto> getCustomerContacts() {
		if (this.customerContacts == null) {
			this.customerContacts = new ArrayList<IncidentTicketNotificationContactDto>();
		}

		return customerContacts;
	}
	
	public List<IncidentTicketNotificationContactDto> getSellerContacts() {
		if (this.sellerContacts == null) {
			this.sellerContacts = new ArrayList<IncidentTicketNotificationContactDto>();
		}

		return sellerContacts;
	}

}
