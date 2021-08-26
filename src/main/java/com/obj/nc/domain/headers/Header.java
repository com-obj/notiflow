package com.obj.nc.domain.headers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.obj.nc.domain.*;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Log4j2
public class Header extends BaseJSONObject implements HasFlowId, HasProcessingInfo {
	
	@JsonProperty("flow-id")
	@Column("flow_id")
	private String flowId;
	
	@Transient
	protected ProcessingInfo processingInfo;
	
	public static final String SUPRESS_GENERATE_PROC_INFO_PARAM_NAME = "SUPRESS_GENERATE_PROCESSING_INFO";

	public void copyHeaderFrom(Header header) {
		if (header == null) {
			return;
		}
		
		BeanUtils.copyProperties(header, this);
	}
	
	@JsonIgnore
	@Transient
	public boolean isSupressGenerateProcessingInfo() {
		if (hasAttribute(SUPRESS_GENERATE_PROC_INFO_PARAM_NAME)) {
			return (Boolean) getAttributeValue(SUPRESS_GENERATE_PROC_INFO_PARAM_NAME);
		}
		
		return false;
	}

}
