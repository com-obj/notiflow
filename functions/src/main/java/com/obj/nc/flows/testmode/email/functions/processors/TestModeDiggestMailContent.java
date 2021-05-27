package com.obj.nc.flows.testmode.email.functions.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;

@JsonTypeName(TestModeDiggestMailContent.JSON_TYPE_IDENTIFIER)
public class TestModeDiggestMailContent extends TemplateWithModelEmailContent<TestModeDiggestModel> {

    public static final String JSON_TYPE_IDENTIFIER = "TEST_MODE_DIGGEST";

	@Override
    public String getContentTypeName() {
    	return JSON_TYPE_IDENTIFIER;
    }
}
