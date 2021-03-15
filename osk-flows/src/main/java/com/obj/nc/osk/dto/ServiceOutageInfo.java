package com.obj.nc.osk.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class ServiceOutageInfo {

	private String productName;
	private String b2bLogin;
	private String sn;
	private String installationAddress;
	
	public static ServiceOutageInfo from(IncidentTicketServiceOutageForCustomerDto serviceOutage) {
		ServiceOutageInfo newInfo = new ServiceOutageInfo();
		newInfo.setB2bLogin(serviceOutage.getB2bLogin());
		newInfo.setProductName(serviceOutage.getService());
		newInfo.setSn(serviceOutage.getSn());
		newInfo.setInstallationAddress(serviceOutage.getInstallationAddress());
		
		return newInfo;
	}
	
}
