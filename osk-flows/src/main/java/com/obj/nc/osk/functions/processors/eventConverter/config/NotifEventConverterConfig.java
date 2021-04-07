package com.obj.nc.osk.functions.processors.eventConverter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.osk.functions.processors.eventConverter.EndOutageEventConverter;
import com.obj.nc.osk.functions.processors.eventConverter.StartOutageEventConverter;
import com.obj.nc.repositories.GenericEventRepository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Configuration
@AllArgsConstructor
@Data
public class NotifEventConverterConfig {	
	
	@Bean
	public StartOutageEventConverter startOutageEventConverter(
			NotifEventConverterConfigProperties props) {
		return new StartOutageEventConverter(props);
	}
	
	@Bean
	public EndOutageEventConverter endOutageEventConverter(
			NotifEventConverterConfigProperties props, 
			GenericEventRepository eventRepo) {
		
		return new EndOutageEventConverter(props,eventRepo);
	}
}
