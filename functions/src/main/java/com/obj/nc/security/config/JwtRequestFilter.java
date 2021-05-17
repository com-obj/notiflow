package com.obj.nc.security.config;

import com.obj.nc.security.exception.UserNotAuthenticatedException;
import com.obj.nc.security.service.JwtUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.obj.nc.security.config.Constants.*;

@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

	private final JwtUserDetailsService jwtUserDetailsService;
	private final JwtTokenUtil jwtTokenUtil;

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
			} catch (IllegalArgumentException | ExpiredJwtException e) {
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
		String requestURI = request.getRequestURI();
		return NOT_PROTECTED_RESOURCES.stream().noneMatch(requestURI::endsWith);
	}

}
