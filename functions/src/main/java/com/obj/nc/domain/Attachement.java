package com.obj.nc.domain;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Attachement {

	String name;
	
	@JsonProperty("file-URI")
    URI fileURI;
	
}
