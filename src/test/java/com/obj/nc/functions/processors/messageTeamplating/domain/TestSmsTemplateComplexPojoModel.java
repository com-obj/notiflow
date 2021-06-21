package com.obj.nc.functions.processors.messageTeamplating.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;

@JsonTypeName("SMS_FROM_TEAMPLATE_COMLEX_MESSAGE_CONTENT")
public class TestSmsTemplateComplexPojoModel extends TemplateWithModelEmailContent<TestModel> {

}

