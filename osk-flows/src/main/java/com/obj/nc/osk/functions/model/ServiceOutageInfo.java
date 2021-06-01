package com.obj.nc.osk.functions.model;

import com.obj.nc.osk.domain.IncidentTicketServiceOutageForCustomerDto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Comparator;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ServiceOutageInfo implements Comparable<ServiceOutageInfo> {

	private String customerName;
	private String productName;
	private String b2bLogin;
	private String sn;
	private String installationAddress;
	private String customerAddress;
	
	public static ServiceOutageInfo from(IncidentTicketServiceOutageForCustomerDto serviceOutage) {
		ServiceOutageInfo newInfo = new ServiceOutageInfo();
		newInfo.setB2bLogin(serviceOutage.getB2bLogin());
		newInfo.setProductName(serviceOutage.getService());
		newInfo.setSn(serviceOutage.getSn());
		newInfo.setInstallationAddress(serviceOutage.getInstallationAddress());
		newInfo.setCustomerName(serviceOutage.getCustomerName());
		newInfo.setCustomerAddress(serviceOutage.getCustomerAddress());
		
		return newInfo;
	}
	
	public CustomerInfo getCustomer() {
		return new CustomerInfo(customerName, customerAddress);
	}
	
	@Override
	public int compareTo(ServiceOutageInfo other) {
		return Comparator.comparing(ServiceOutageInfo::getCustomerName)
				.thenComparing(ServiceOutageInfo::getB2bLogin)
				.thenComparing(ServiceOutageInfo::getProductName)
				.compare(this, other);
	}
}
