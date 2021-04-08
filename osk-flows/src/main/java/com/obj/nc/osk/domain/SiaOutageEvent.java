package com.obj.nc.osk.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(of = "id")
@JsonTypeInfo(include = As.PROPERTY, use = Id.NAME)
@NoArgsConstructor
public abstract class SiaOutageEvent {
	Long id;

}
