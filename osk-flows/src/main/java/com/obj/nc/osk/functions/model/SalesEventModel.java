package com.obj.nc.osk.functions.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class SalesEventModel {
	
	final private Date timeStart;
	
	final private Date timeEnd;
	
	final private Map<CustomerInfo, List<ServiceOutageInfo>> servicesPerCustomer;
	
	final private Integer customerCount;
}
