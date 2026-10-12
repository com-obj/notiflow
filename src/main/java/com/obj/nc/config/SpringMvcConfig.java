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

package com.obj.nc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.security.model.AuthenticationError;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.UrlHandlerFilter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class SpringMvcConfig {

    @Bean
    public FilterRegistrationBean<UrlHandlerFilter> trailingSlashFilter() {
        var filter = UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build();
        var registration = new FilterRegistrationBean<>(filter);
        // Restore trailing-slash URLs before Security and MVC make decisions about the request path.
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest request, ErrorAttributeOptions options) {
                Map<String, Object> attributes = super.getErrorAttributes(request, options);
                // Boot 2 included an empty message when details were hidden. Preserve that JSON shape.
                attributes.putIfAbsent("message", "");
                return attributes;
            }
        };
    }

    @ExceptionHandler({PayloadValidationException.class})
    ResponseEntity<String> handleMethodArgumentNotValidException(PayloadValidationException e) {
        return new ResponseEntity<>("Request not valid because of invalid payload: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({MethodArgumentNotValidException.class})
    ResponseEntity<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>("Request arguments not valid: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({HttpMessageNotReadableException.class})
    ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new ResponseEntity<>("Request arguments not valid: " + e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        return new ResponseEntity<>(AuthenticationError.builder().timestamp(Timestamp.valueOf(LocalDateTime.now()))
                .message(e.getMessage()).build().toString(), HttpStatus.UNAUTHORIZED
        );
    }
    
    @ExceptionHandler({IllegalArgumentException.class})
    ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler({ResponseStatusException.class})
    ResponseEntity<String> handleResponseStatusException(ResponseStatusException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.valueOf(e.getStatusCode().value()));
    }
    
    @ExceptionHandler({RuntimeException.class})
    ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return new ResponseEntity<>("Unexpected error ocurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @Bean
    @Primary
    //there is another objectMapper with different setting used for message serialization. this is used for REST Controllers
    public ObjectMapper objectMapper() {       
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
