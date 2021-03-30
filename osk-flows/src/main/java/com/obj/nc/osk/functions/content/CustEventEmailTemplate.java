package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.message.TemplateWithModelBasedContent;
import com.obj.nc.osk.functions.model.CustEventModel;

@JsonTypeName("CUST_EVENT")
public class CustEventEmailTemplate extends TemplateWithModelBasedContent<CustEventModel> {
    
}
