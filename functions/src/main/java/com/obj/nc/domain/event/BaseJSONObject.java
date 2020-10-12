package com.obj.nc.domain.event;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import lombok.Data;

@Data
public class BaseJSONObject {

	private Map<String, String> attributes = new HashMap<String, String>();

	@JsonAnySetter
	public void pubAttributeValue(String key, String value) {
		attributes.put(key, value);
	}

	public static String generateUUID() {
		try {
			MessageDigest salt = MessageDigest.getInstance("SHA-256");
			salt.update(UUID.randomUUID().toString().getBytes("UTF-8"));
			return bytesToHex(salt.digest());
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

	}

	public static String bytesToHex(byte[] hash) {
		StringBuffer hexString = new StringBuffer();
		for (byte h : hash) {
			String hex = Integer.toHexString(0xff & h);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

}
