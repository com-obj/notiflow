package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.osk.functions.model.CustEventModel;
import lombok.NonNull;

@JsonTypeName("CUST_EMAIL")
public class CustEmailTemplate extends TemplateWithModelEmailContent<CustEventModel> {
    
    @Override
    public @NonNull String[] getSubjectResourcesMessageParameters() {
        return new String[] { getModel().getCustomerName() };
    }
    
}
