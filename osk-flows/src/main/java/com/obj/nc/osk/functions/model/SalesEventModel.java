package com.obj.nc.osk.functions.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class SalesEventModel {
	
	@NotNull
	final private Date timeStart;
	
	@NotNull
	final private Date timeEnd;
	
	@NotNull
	final private Map<CustomerInfo, List<ServiceOutageInfo>> servicesPerCustomer;
}
