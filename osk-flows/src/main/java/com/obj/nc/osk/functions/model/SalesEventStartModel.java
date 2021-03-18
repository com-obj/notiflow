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
public class SalesEventStartModel {
	
	@NotNull
	final private Date timeStart;
	
	@NotNull
	final private Map<String, List<ServiceOutageInfo>> servicesPerCustomer;
}
