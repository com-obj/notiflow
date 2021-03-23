package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.message.BaseEmailFromTemplate;
import com.obj.nc.osk.functions.model.CustEventStartModel;

@JsonTypeName("CUST_EVENT_START")
public class CustEventStartEmailTemplate extends BaseEmailFromTemplate<CustEventStartModel> {

    @Override
    public String getContentTypeName() {
        return "CUST_EVENT_START";
    }

}
