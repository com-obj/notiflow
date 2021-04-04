package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.osk.functions.model.SalesEventModel;

@JsonTypeName("SALES_EMAIL")
public class SalesEmailTemplate extends TemplateWithModelEmailContent<SalesEventModel> {
    
}