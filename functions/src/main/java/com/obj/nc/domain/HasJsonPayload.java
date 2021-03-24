package com.obj.nc.domain;

import com.fasterxml.jackson.databind.JsonNode;

public interface HasJsonPayload {

	public JsonNode getPayloadJson();
}
