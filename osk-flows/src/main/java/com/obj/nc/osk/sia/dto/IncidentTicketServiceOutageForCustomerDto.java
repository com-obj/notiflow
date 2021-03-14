package com.obj.nc.osk.sia.dto;

import java.util.List;

/**
 * Don't change, this is part of communication protocol with SIA application
 * @author ja
 *
 */
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
	String sn;
	String installationAddress;

	List<IncidentTicketNotificationContactDto> customerContacts;
	List<IncidentTicketNotificationContactDto> sellerContacts;

	public CustomerSegment getCustomerSegment() {
		return customerSegment;
	}

	public void setCustomerSegment(CustomerSegment customerSegment) {
		this.customerSegment = customerSegment;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public void setResourceId(Long resourceId) {
		this.resourceId = resourceId;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public Long getBillingAccountId() {
		return billingAccountId;
	}

	public void setBillingAccountId(Long billingAccountId) {
		this.billingAccountId = billingAccountId;
	}

	public String getBillingAccount() {
		return billingAccount;
	}

	public void setBillingAccount(String billingAccount) {
		this.billingAccount = billingAccount;
	}

	public Long getSubscriptionId() {
		return subscriptionId;
	}

	public void setSubscriptionId(Long subscriptionId) {
		this.subscriptionId = subscriptionId;
	}

	public String getSubscription() {
		return subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getB2bLogin() {
		return b2bLogin;
	}

	public void setB2bLogin(String b2bLogin) {
		this.b2bLogin = b2bLogin;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getInstallationAddress() {
		return installationAddress;
	}

	public void setInstallationAddress(String installationAddress) {
		this.installationAddress = installationAddress;
	}

	public List<IncidentTicketNotificationContactDto> getCustomerContacts() {
		return customerContacts;
	}

	public void setCustomerContacts(List<IncidentTicketNotificationContactDto> customerContacts) {
		this.customerContacts = customerContacts;
	}

	public List<IncidentTicketNotificationContactDto> getSellerContacts() {
		return sellerContacts;
	}

	public void setSellerContacts(List<IncidentTicketNotificationContactDto> sellerContacts) {
		this.sellerContacts = sellerContacts;
	}

}
