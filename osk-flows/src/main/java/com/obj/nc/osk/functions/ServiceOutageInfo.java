package com.obj.nc.osk.functions;

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
	
}
