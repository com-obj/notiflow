package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.message.BaseEmailFromTemplate;
import com.obj.nc.osk.functions.model.SalesEventModel;

@JsonTypeName("SALES_EVENT")
public class SalesEventEmailTemplate extends BaseEmailFromTemplate<SalesEventModel> {
    
    @Override
    public String getContentTypeName() {
        return "SALES_EVENT";
    }
    
}
