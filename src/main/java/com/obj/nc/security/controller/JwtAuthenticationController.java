package com.obj.nc.security.controller;

import com.obj.nc.security.config.Constants;
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
import org.springframework.web.bind.annotation.*;

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

	@PostMapping(value = Constants.API.AUTHENTICATE, consumes = MediaType.APPLICATION_JSON_VALUE, 
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
