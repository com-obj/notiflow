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

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.DefaultPropertiesPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * Supplies the Spring Boot settings Notiflow depends on as lowest-precedence defaults. A consuming
 * application's own application.properties hides the library's copy, so these must not live there.
 */
public class CompatibilityDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    static final Map<String, Object> DEFAULTS = Map.of(
            // Existing integration configurations depend on the circular-reference behavior of Boot 2.
            "spring.main.allow-circular-references", "true",
            // Preserve existing JSON contracts and stored message payloads (Jackson 2).
            "spring.http.converters.preferred-json-mapper", "jackson2");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DefaultPropertiesPropertySource.addOrMerge(DEFAULTS, environment.getPropertySources());
    }
}
