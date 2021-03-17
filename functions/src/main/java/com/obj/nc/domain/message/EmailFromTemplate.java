package com.obj.nc.domain.message;

import com.obj.nc.domain.BaseJSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class EmailFromTemplate extends BaseEmailFromTemplate<BaseJSONObject> {
	
	public final static String JSON_TYPE_IDENTIFIER = "EMAIL_FROM_TEAMPLATE_MESSAGE_CONTENT";

}
