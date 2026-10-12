/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Data
@Component
@RequiredArgsConstructor
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -7573345844238770803L;

	public static final long JWT_TOKEN_VALIDITY = 5*60*60;

	private final NcJwtConfigProperties ncJwtConfigProperties;

	@PostConstruct
	public void validateSignatureSecret() {
		// Reject an incompatible secret at startup rather than on the first authenticated request.
		if (ncJwtConfigProperties.isEnabled()) {
			if (ncJwtConfigProperties.getSignatureSecret() == null) {
				throw new IllegalStateException("nc.jwt.signature-secret must be set when nc.jwt.enabled=true");
			}
			signingKey(ncJwtConfigProperties.getSignatureSecret());
		}
	}

	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	public Date getIssuedAtDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getIssuedAt);
	}

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().verifyWith(signingKey(ncJwtConfigProperties.getSignatureSecret())).build().parseSignedClaims(token).getPayload();
	}

	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	private Boolean ignoreTokenExpiration(String token) {
		// here you specify tokens, for that the expiration is ignored
		return false;
	}

	public String generateToken(UserDetails userDetails, String signature) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, userDetails.getUsername(), signature);
	}

	private String doGenerateToken(Map<String, Object> claims, String subject, String signature) {

		return Jwts.builder().claims(claims).subject(subject).issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY*1000))
				.signWith(signingKey(signature), Jwts.SIG.HS512).compact();
	}

	private SecretKey signingKey(String signature) {
		// JJWT 0.9 interpreted String keys as Base64; retain that format for existing deployments.
		byte[] key = Decoders.BASE64.decode(signature);
		if (key.length < 64) {
			throw new IllegalArgumentException("nc.jwt.signature-secret must be Base64 encoding of at least 64 bytes for HS512");
		}
		return new SecretKeySpec(key, "HmacSHA512");
	}

	public Boolean canTokenBeRefreshed(String token) {
		return (!isTokenExpired(token) || ignoreTokenExpiration(token));
	}

	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
}
