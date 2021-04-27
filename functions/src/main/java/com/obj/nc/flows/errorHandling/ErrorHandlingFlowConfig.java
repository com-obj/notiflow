package com.obj.nc.flows.errorHandling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class ErrorHandlingFlowConfig {
	

	//Default channel for errorMessages used by spring
	@Autowired
	@Qualifier("errorChannel")
	private PublishSubscribeChannel errorChannel;

}
