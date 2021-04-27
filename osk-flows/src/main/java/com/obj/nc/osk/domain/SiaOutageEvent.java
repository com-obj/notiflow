package com.obj.nc.osk.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.obj.nc.domain.IsTypedJson;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(of = "id")
@JsonTypeInfo(include = As.PROPERTY, use = Id.NAME)
@JsonSubTypes({ 
	@Type(value = IncidentTicketOutageEndEventDto.class, name = "OUTAGE_END"),
	@Type(value = IncidentTicketOutageStartEventDto.class, name = "OUTAGE_START") })
@NoArgsConstructor
public abstract class SiaOutageEvent implements IsTypedJson {
	String id;

}
