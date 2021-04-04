package com.obj.nc.flows.testmode.email.functions.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;

@JsonTypeName("TEST_MODE_DIGGEST")
public class TestModeDiggestMailContent extends TemplateWithModelEmailContent<TestModeDiggestModel> {

}
