package com.obj.nc.osk.domain.incidentTicket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@JsonTypeInfo(include = As.PROPERTY, use = Id.NAME)
public abstract class SiaOutageEvent {
	Long id;

}
