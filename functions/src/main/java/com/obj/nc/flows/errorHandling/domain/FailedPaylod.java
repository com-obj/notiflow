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
public class FailedPaylod implements Persistable<UUID>, HasFlowId {
	
	@Id
	private UUID id;
	private String flowId;
	//sem ukladam celu spring message. Je otazne ci potrebujem robit takuto zavyslost na spring. Ciastocne je to aj nebezpecne lebo v headri mozu byt veci, ktore neviem
	//zoserializovat. Na druhej strane sa bojim, ze ak dam len payload tak stratim veci ktore mi budu chybat. Mozno by bolo treba ukladat iba payload, spravit novy stlpec
	//kam budem ukladat whitelistovane atributy z headra
	private JsonNode messageJson;
	
	private String exceptionName;
	private String errorMessage;
	private String stackTrace;
	private String rootCauseExceptionName;
	
	private String channelNameForRetry;
	
	@CreatedDate
	private Instant timeCreated;
	//failed message resurection attempt
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
