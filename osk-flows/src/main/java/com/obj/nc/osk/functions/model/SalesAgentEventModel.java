package com.obj.nc.osk.functions.model;

import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class SalesAgentEventModel {
	
	final private Date timeStart;
	
	final private Date timeEnd;

	final private List<ServiceOutageInfo> services;
	
	final private Integer customerCount;
}
