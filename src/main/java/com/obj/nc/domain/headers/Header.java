/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.domain.headers;

import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.obj.nc.domain.BaseJSONObject;
import com.obj.nc.domain.HasFlowId;
import com.obj.nc.domain.HasProcessingInfo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Header extends BaseJSONObject implements HasFlowId, HasProcessingInfo {
	
	@JsonProperty("flow-id")
	@Column("flow_id")
	private String flowId;
	
	@Transient
	protected ProcessingInfo processingInfo;
	
	public static final String SUPPRESS_GENERATE_PROC_INFO_PARAM_NAME = "SUPPRESS_GENERATE_PROCESSING_INFO";

	public void copyHeaderFrom(Header header) {
		if (header == null) {
			return;
		}
		
		BeanUtils.copyProperties(header, this);
	}
	
	@JsonIgnore
	@Transient
	public boolean isSuppressGenerateProcessingInfo() {
		if (hasAttribute(SUPPRESS_GENERATE_PROC_INFO_PARAM_NAME)) {
			return (Boolean) getAttributeValue(SUPPRESS_GENERATE_PROC_INFO_PARAM_NAME);
		}
		
		return false;
	}

}
