package com.obj.nc.security.config;

import static com.obj.nc.security.config.Constants.DEFAULT_EXCEPTION_MSG;
import static com.obj.nc.security.config.Constants.EXCEPTION_ATTR_NAME;
import static com.obj.nc.security.config.Constants.NOT_PROTECTED_RESOURCES;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.obj.nc.security.model.AuthenticationError;

import lombok.RequiredArgsConstructor;

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
		httpSecurity.csrf().disable()
				.authorizeRequests()
				.antMatchers(NOT_PROTECTED_RESOURCES.toArray(new String[0])).permitAll()
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


