package com.obj.nc.security.model;

import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtRequest {
	
	private String username;
	private String password;
	
	@Override
	public String toString() {
		return JsonUtils.writeObjectToJSONString(this);
	}
	
}