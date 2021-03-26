package com.obj.nc.flows.testmode.functions.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.message.BaseEmailFromTemplate;

@JsonTypeName("TEST_MODE_DIGGEST")
public class TestModeDiggestMailContent extends BaseEmailFromTemplate<TestModeDiggestModel> {

	@Override
	public String getContentTypeName() {
		return "TEST_MODE_DIGGEST";
	}

}
