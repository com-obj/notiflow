package com.obj.nc.flows.testmode.functions.processors;

import java.util.ArrayList;
import java.util.List;

import com.obj.nc.domain.content.email.EmailContent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
public class TestModeDiggestModel {
	
	private List<EmailContent> emailContents = new ArrayList<>();

}
