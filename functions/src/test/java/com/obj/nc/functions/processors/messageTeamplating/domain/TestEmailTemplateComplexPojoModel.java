package com.obj.nc.functions.processors.messageTeamplating.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;

@JsonTypeName("EMAIL_FROM_TEAMPLATE_COMLEX_MESSAGE_CONTENT")
public class TestEmailTemplateComplexPojoModel extends TemplateWithModelEmailContent<TestModel> {

}
