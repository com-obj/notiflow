package com.obj.nc.flows.testmode.functions.processors;

import java.util.ArrayList;
import java.util.List;

import com.obj.nc.domain.message.Email;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class TestModeDiggestModel {
	
	private List<Email> emails = new ArrayList<>();

}
