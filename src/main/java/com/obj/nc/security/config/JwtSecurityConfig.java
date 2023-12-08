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

import com.obj.nc.security.model.AuthenticationError;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.http.HttpServletResponse;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;

import static com.obj.nc.security.config.Constants.DEFAULT_EXCEPTION_MSG;
import static com.obj.nc.security.config.Constants.EXCEPTION_ATTR_NAME;
import static com.obj.nc.security.config.Constants.UNPROTECTED_PATHS;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "nc.jwt.enabled", havingValue = "true")
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private final JwtRequestFilter jwtRequestFilter;

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity
				.cors().configurationSource(request -> {
					CorsConfiguration cors = new CorsConfiguration();
					cors.setAllowedOrigins(Arrays.asList("http://localhost:9000"));
					cors.setAllowedMethods(Arrays.asList("*"));
					cors.setAllowedHeaders(Arrays.asList("*"));
					return cors;
				}).and()
				.csrf().disable()
				.authorizeRequests()
				.antMatchers(UNPROTECTED_PATHS).permitAll()
				.anyRequest().authenticated()
				.and()
				.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		
		httpSecurity.exceptionHandling().authenticationEntryPoint((request, response, exception) -> {
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			
			Exception exceptionAttribute = (Exception) request.getAttribute(EXCEPTION_ATTR_NAME);
			String exceptionMessage = exceptionAttribute == null ? DEFAULT_EXCEPTION_MSG : exceptionAttribute.getMessage();
			
			response.getWriter().write(AuthenticationError.builder()
					.timestamp(Timestamp.valueOf(LocalDateTime.now()))
					.message(exceptionMessage)
					.build().toString()
			);
		});
	}

}
