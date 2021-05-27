package com.obj.nc.osk.functions.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.email.TemplateWithModelEmailContent;
import com.obj.nc.osk.functions.model.CustEventModel;

import lombok.NonNull;

@JsonTypeName("CUST_EMAIL")
public class CustEmailTemplate extends TemplateWithModelEmailContent<CustEventModel> {
    
    public static final String JSON_TYPE_IDENTIFIER = "CUST_EMAIL";

	@Override
    public @NonNull String[] getSubjectResourcesMessageParameters() {
        return new String[] { getModel().getCustomerName() };
    }
    
	@Override
	public String getContentTypeName() {
		return JSON_TYPE_IDENTIFIER;
	}
}
