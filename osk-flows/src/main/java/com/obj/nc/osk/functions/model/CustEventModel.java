package com.obj.nc.osk.functions.model;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class CustEventModel {
	
	@NotNull
	final private Date timeStart;
	
	@NotNull
	final private Date timeEnd;
	
	@NotNull
	final private String customerName;
	
	@NotNull
	final private List<ServiceOutageInfo> services;
}
