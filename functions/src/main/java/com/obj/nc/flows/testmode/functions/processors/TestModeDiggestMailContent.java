package com.obj.nc.flows.testmode.functions.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.message.TemplateWithModelBasedContent;

@JsonTypeName("TEST_MODE_DIGGEST")
public class TestModeDiggestMailContent extends TemplateWithModelBasedContent<TestModeDiggestModel> {

}
