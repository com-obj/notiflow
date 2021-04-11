package com.obj.nc.osk.functions.processors.eventConverter.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.Get;
import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.osk.domain.SiaOutageEvent;
import com.obj.nc.osk.functions.processors.eventConverter.EndOutageEventConverter;
import com.obj.nc.osk.functions.processors.eventConverter.StartOutageEventConverter;
import com.obj.nc.repositories.GenericEventRepository;
import com.obj.nc.utils.JsonUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Configuration
@AllArgsConstructor
@Data
public class NotifEventConverterConfig {	
	
	@Autowired Get get;
	
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
	
    @PostConstruct
    public void registerJsonMixins() {
    	JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, SiaOutageEvent.class);
    }
}
