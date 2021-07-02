package com.obj.nc.domain.content.sms;

import com.obj.nc.domain.content.MessageContent;

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
public class SimpleTextContent extends MessageContent {
	
	@EqualsAndHashCode.Include
	private String text;

}
