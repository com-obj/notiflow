package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.message.BaseEmailFromTemplate;
import com.obj.nc.osk.functions.model.CustEventStartModel;
import com.obj.nc.osk.functions.model.SalesEventStartModel;

@JsonTypeName("SALES_EVENT_START")
public class SalesEventStartEmailTemplate extends BaseEmailFromTemplate<SalesEventStartModel> {

    @Override
    public String getContentTypeName() {
        return "SALES_EVENT_START";
    }

}
