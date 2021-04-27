package com.obj.nc.flows.errorHandling.domain;

import java.time.Instant;
import java.util.UUID;

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
public class FailedPaylod implements Persistable<UUID>, HasFlowId {
	
	@Id
	private UUID id;
	private String flowId;
	
	private JsonNode payloadJson;
	
	private String exceptionName;
	private String errorMessage;
	private String stackTrace;
	private String rootCauseExceptionName;
	
	@CreatedDate
	private Instant timeCreated;
	//failed message resurection attempt
	private Instant timeResurected;
	

	@Override
	public boolean isNew() {
		return timeCreated == null;
	}
	
}
