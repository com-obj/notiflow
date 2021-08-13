package com.obj.nc.security.config;

import java.util.List;

import static java.util.Arrays.asList;

public interface Constants {
    
    String AUTHORIZATION_HEADER = "Authorization";
    String JWT_TOKEN_PREFIX = "Bearer ";
    String EXCEPTION_ATTR_NAME = "javax.servlet.error.exception";
    String DEFAULT_EXCEPTION_MSG = "Authentication failed";
    
    List<String> NOT_PROTECTED_RESOURCES = asList(
            "/authenticate", 
            "/delivery-info/messages/read/*"
    );
    
}
