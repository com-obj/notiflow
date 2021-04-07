package com.obj.nc.osk.functions.processors.sms.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.functions.processors.senders.SmsSender;
import com.obj.nc.osk.functions.processors.sms.OskSmsSenderRestImpl;

import lombok.AllArgsConstructor;
import lombok.Data;

@Configuration
@AllArgsConstructor
@Data
public class OskSmsSenderConfig {	
	
	@Bean
	public SmsSender smsSender(
			OskSmsSenderConfigProperties properties, 
			RestTemplateBuilder restTemplateBuilder) {
		return new OskSmsSenderRestImpl(properties, restTemplateBuilder);
	}

}
