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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.router.AbstractMessageRouter;

import com.obj.nc.flows.inputEventRouting.InputEventRouter;
import com.obj.nc.functions.sources.genericEvents.GenericEventsSupplier;

@Configuration
public class InputEventRoutingFlowConfig {
		
	@Autowired private InputEventRoutingProperties routingProps;	
//	@Autowired private GenericEventsSupplier genericEventSupplier;	
	
    public static final String GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME = "genericEventSupplierFlowId"; 
    
    @Bean
    public IntegrationFlow inputEventRoutingFlow() {
    	return IntegrationFlows
			.fromSupplier(genericEventSupplier(), 
					conf-> conf.poller(Pollers.fixedRate(routingProps.getPollPeriodInMiliSeconds()))
					.id(GENERIC_EVENT_CHANNEL_ADAPTER_BEAN_NAME))
			.channel(new DirectChannel())
			.route(inputEventRouter())
			.get();
    }

    @Bean
    public AbstractMessageRouter inputEventRouter() {
        return new InputEventRouter(); 
    }   
    
    @Bean
    public GenericEventsSupplier genericEventSupplier() {
    	return new GenericEventsSupplier();
    }

}
