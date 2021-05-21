package com.obj.nc.domain.content.sms;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.HasRecievingEndpoints;
import com.obj.nc.domain.content.Content;
import com.obj.nc.domain.endpoints.SmsEndpoint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@JsonTypeName(SimpleTextContent.JSON_TYPE_IDENTIFIER)
public class SimpleTextContent extends Content implements HasRecievingEndpoints {
	
	public final static String JSON_TYPE_IDENTIFIER = "TEXT_CONTENT";

	@EqualsAndHashCode.Include
	private String text;

	@EqualsAndHashCode.Include
	private List<SmsEndpoint> recievingEndpoints = new ArrayList<SmsEndpoint>();
}
