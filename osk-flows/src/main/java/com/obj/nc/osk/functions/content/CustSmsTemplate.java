package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.TemplateWithModelContent;
import com.obj.nc.osk.functions.model.CustEventModel;

@JsonTypeName("CUST_SMS")
public class CustSmsTemplate extends TemplateWithModelContent<CustEventModel> {
    
}
