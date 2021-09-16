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

package com.obj.nc.flows.inputEventRouting.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = InputEventRoutingProperties.CONFIG_PROPS_PREFIX)
public class InputEventRoutingProperties {
			
	public static final String CONFIG_PROPS_PREFIX = "nc.flows.input-evet-routing";

    private int pollPeriodInMiliSeconds=1000;
    
    /*if type = PAYLOAD_TYPE. Map<son @type value, channel name>*/
    private Map<String,String> typeChannelMapping;
    
    /*if type = PAYLOAD_TYPE. */
    private String typeProperyName = "@routing-type";

}
