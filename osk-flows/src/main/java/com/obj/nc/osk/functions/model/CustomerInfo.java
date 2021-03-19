package com.obj.nc.osk.functions.model;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(of = "customerName")
@AllArgsConstructor
@RequiredArgsConstructor
public class CustomerInfo {
	
	@NotNull
	private final String customerName;
	
	private String customerAddress;

}
