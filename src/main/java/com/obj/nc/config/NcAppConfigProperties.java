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

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("nc.app")
public class NcAppConfigProperties {
    
    private String url;
    private boolean checkReferenceIntegrity = false;

    @Value("${nc.app.executor.core-pool-size:10}")
    private int corePoolSize;

    @Value("${nc.app.executor.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${nc.app.executor.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${nc.app.executor.block-policy-wait-time:5}")
    private int blockPolicyWaitTimeInSec;

    @Value("${nc.app.url.context-path:/notiflow}")
    private String contextPath;

}
