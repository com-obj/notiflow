package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.osk.functions.model.CustEventModel;

@JsonTypeName("CUST_EVENT")
public class CustEventEmailTemplate extends TemplateWithModelEmailContent<CustEventModel> {
    
}
