package com.obj.nc.security.exception;

import org.springframework.security.core.AuthenticationException;

public class UserNotAuthenticatedException extends AuthenticationException {
    
    public UserNotAuthenticatedException(String s) {
        super(s);
    }
    
}
