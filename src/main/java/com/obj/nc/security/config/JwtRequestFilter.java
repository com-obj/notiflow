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

import static com.obj.nc.security.config.Constants.AUTHORIZATION_HEADER;
import static com.obj.nc.security.config.Constants.JWT_TOKEN_PREFIX;
import static com.obj.nc.security.config.Constants.NOT_PROTECTED_RESOURCES;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.obj.nc.config.NcAppConfigProperties;
import com.obj.nc.security.exception.UserNotAuthenticatedException;
import com.obj.nc.security.service.JwtUserDetailsService;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnBean(JwtSecurityConfig.class)
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final JwtUserDetailsService jwtUserDetailsService;
	private final JwtTokenUtil jwtTokenUtil;
	private final AntPathMatcher antPathMatcher;
	private final NcAppConfigProperties ncAppConfigProperties;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String requestTokenHeader = request.getHeader(AUTHORIZATION_HEADER);
		String username = null;
		String jwtToken = null;
		
		if (!isProtectedResource(request)) {
			chain.doFilter(request, response);
			return;
		}

		if (requestTokenHeader != null && requestTokenHeader.startsWith(JWT_TOKEN_PREFIX)) {
			jwtToken = requestTokenHeader.substring(JWT_TOKEN_PREFIX.length());
			try {
				username = jwtTokenUtil.getUsernameFromToken(jwtToken);
			} catch (Exception e) {
				throw new UserNotAuthenticatedException(e.getMessage());
			}
		} else {
			throw new UserNotAuthenticatedException("JWT Token does not begin with Bearer String");
		}

		if (username != null) {
			UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
			
			Boolean validated = jwtTokenUtil.validateToken(jwtToken, userDetails);
			if (validated) {
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			} else {
				throw new UserNotAuthenticatedException("JWT Token validation failed");
			}
		}
		
		chain.doFilter(request, response);
	}
	
	private boolean isProtectedResource(HttpServletRequest request) {
		return NOT_PROTECTED_RESOURCES.stream()
				.map(resource -> ncAppConfigProperties.getContextPath() + resource)
				.noneMatch(resource -> antPathMatcher.match(resource, request.getRequestURI()));
	}

}
