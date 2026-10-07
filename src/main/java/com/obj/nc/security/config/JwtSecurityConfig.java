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

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import static com.obj.nc.security.config.Constants.UNPROTECTED_PATHS;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "nc.jwt.enabled", havingValue = "true")
public class JwtSecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final CorsConfigProperties corsConfigProperties;

    @Bean
    @Order(2)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    corsConfigProperties.getAllowedOrigins().forEach(configuration::addAllowedOrigin);
                    corsConfigProperties.getAllowedMethods().forEach(configuration::addAllowedMethod);
                    corsConfigProperties.getAllowedHeaders().forEach(configuration::addAllowedHeader);
                    return configuration;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests -> requests
                        // Preserve the controller's status on a servlet error dispatch after JWT authentication.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(UNPROTECTED_PATHS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .build();
    }

    @Bean
    public FilterRegistrationBean<JwtRequestFilter> jwtFilterRegistration() {
        // GAP callbacks use Basic authentication and must not run through the JWT filter.
        var registration = new FilterRegistrationBean<>(jwtRequestFilter);
        registration.setEnabled(false);
        return registration;
    }
}
