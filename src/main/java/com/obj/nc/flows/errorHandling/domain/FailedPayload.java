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

package com.obj.nc.flows.errorHandling.domain;

import java.time.Instant;
import java.util.UUID;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.obj.nc.domain.HasFlowId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Table("nc_failed_payload")
@Builder
public class FailedPayload implements Persistable<UUID>, HasFlowId {
	
	@Id
	private UUID id;
	private String flowId;

	//this contains the full spring message. I'm not sure If I need this strong dependency. It might be event dangerous because there might be things in header which I cannot serialize.
	//on the other hand, I'm afraid to loos things in the header which might be important. Maybe we should store payload in one attribute and whitelisted attributes from header in another. 
	private JsonNode messageJson;
	
	private String exceptionName;
	private String errorMessage;
	private String stackTrace;
	private String rootCauseExceptionName;
	
	private String channelNameForRetry;
	
	@CreatedDate
	private Instant timeCreated;
	//failed message resurrection attempt
	private Instant timeResurected;
	
	@Override
	public boolean isNew() {
		return timeCreated == null;
	}
	
	public void setAttributesFromException(Throwable e) {
		setExceptionName(e.getClass().getName());
		setErrorMessage(e.getMessage());
		setStackTrace(ExceptionUtils.getStackTrace(e));
		setRootCauseExceptionName(ExceptionUtils.getRootCause(e).getClass().getName());
	}
	
}
