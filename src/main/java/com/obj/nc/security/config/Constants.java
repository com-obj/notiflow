package com.obj.nc.security.config;

import static java.util.Arrays.asList;

import java.util.List;

public interface Constants {
    
    String AUTHORIZATION_HEADER = "Authorization";
    String JWT_TOKEN_PREFIX = "Bearer ";
    String EXCEPTION_ATTR_NAME = "javax.servlet.error.exception";
    String DEFAULT_EXCEPTION_MSG = "Authentication failed";
    
    List<String> NOT_PROTECTED_RESOURCES = asList(
            "/authenticate", 
            "/delivery-info/messages/*/mark-as-read",
            "/resources/images/px.png"
    );
    
}
