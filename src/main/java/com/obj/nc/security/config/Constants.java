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

import java.util.List;

import static java.util.Arrays.asList;

public interface Constants {
    
    String AUTHORIZATION_HEADER = "Authorization";
    String JWT_TOKEN_PREFIX = "Bearer ";
    String EXCEPTION_ATTR_NAME = "javax.servlet.error.exception";
    String DEFAULT_EXCEPTION_MSG = "Authentication failed";

    String[] UNPROTECTED_PATHS = {
            "/authenticate",
            "/delivery-info/messages/*/mark-as-read",
            "/resources/images/px.png",
            "/v2/api-docs",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**"
    };
    
}
