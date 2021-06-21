package com.obj.nc.domain;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Attachement {

	String name;
	
	@JsonProperty("file-URI")
    URI fileURI;
	
	@JsonProperty("file-name")
    String filePathAndName;
	
}
