package com.obj.nc.domain;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Header extends BaseJSONObject {
	
	@JsonProperty("configuration-name")
	private String configurationName;
		
	@NotNull
	@Include
	private UUID id;
	
	@NotNull
	@Include
	private UUID eventId;

	public void generateAndSetID() {
		id = generateUUID();
	}

	public void copyHeaderFrom(Header header) {
		BeanUtils.copyProperties(header, this);
	}

}
