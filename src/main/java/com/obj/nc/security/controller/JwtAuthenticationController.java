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

package com.obj.nc.security.controller;

import com.obj.nc.security.config.JwtSecurityConfig;
import com.obj.nc.security.config.JwtTokenUtil;
import com.obj.nc.security.config.NcJwtConfigProperties;
import com.obj.nc.security.exception.UserNotAuthenticatedException;
import com.obj.nc.security.model.JwtRequest;
import com.obj.nc.security.model.JwtResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Log4j2
@RestController
@CrossOrigin
@ConditionalOnBean(JwtSecurityConfig.class)
@RequiredArgsConstructor
public class JwtAuthenticationController {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final UserDetailsService jwtInMemoryUserDetailsService;
	private final NcJwtConfigProperties ncJwtConfigProperties;

	@PostMapping(value = "/authenticate", consumes = MediaType.APPLICATION_JSON_VALUE, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
		authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

		final UserDetails userDetails = jwtInMemoryUserDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());
		
		final String token = jwtTokenUtil.generateToken(userDetails, ncJwtConfigProperties.getSignatureSecret());
		return ResponseEntity.ok(new JwtResponse(token));
	}

	private void authenticate(String username, String password) {
		Objects.requireNonNull(username);
		Objects.requireNonNull(password);

		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (AuthenticationException e) {
			log.warn(String.format("Authentication error with user %s: %s", username, e.getMessage()));
			throw new UserNotAuthenticatedException(e.getMessage());
		}
		
		log.info(String.format("Authenticated user: %s", username));
	}
}
