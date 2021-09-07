package com.obj.nc.security.model;

import java.sql.Timestamp;

import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationError {

	private Timestamp timestamp;
	private String message;
	
	@Override
	public String toString() {
		return JsonUtils.writeObjectToJSONString(this);
	}
	
}