package com.obj.nc.domain.content.sms;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.obj.nc.domain.content.Content;

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
public class SimpleTextContent extends Content {
	
	public final static String JSON_TYPE_IDENTIFIER = "TEXT_CONTENT";

	@EqualsAndHashCode.Include
	private String text;

}