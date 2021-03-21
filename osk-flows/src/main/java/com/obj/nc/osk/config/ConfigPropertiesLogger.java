package com.obj.nc.osk.config;

import java.util.Arrays;
import java.util.Properties;
import java.util.stream.StreamSupport;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ConfigPropertiesLogger {
	
	@EventListener
	public void handleContextRefreshed(ContextRefreshedEvent event) {
		printActiveProperties(event.getApplicationContext().getEnvironment());
	}

	private void printActiveProperties(Environment env) {

		log.info("************************* ACTIVE APP PROPERTIES ******************************");
		Properties props = new Properties();
		MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
		StreamSupport.stream(propSrcs.spliterator(), false)
		        .filter(ps -> ps instanceof EnumerablePropertySource)
		        .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
		        .flatMap(Arrays::<String>stream)
		        .forEach(propName -> props.setProperty(propName, env.getProperty(propName)));
		log.info("******************************************************************************");
	}
}
