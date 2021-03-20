package com.obj.nc.osk.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ConfigPropertiesLogger {
	
	@EventListener
	public void handleContextRefreshed(ContextRefreshedEvent event) {
		printActiveProperties((ConfigurableEnvironment) event.getApplicationContext().getEnvironment());
	}

	private void printActiveProperties(ConfigurableEnvironment env) {

		log.info("************************* ACTIVE APP PROPERTIES ******************************");

		List<MapPropertySource> propertySources = new ArrayList<>();

		env.getPropertySources().forEach(it -> {
			if (it instanceof MapPropertySource && it.getName().contains("applicationConfig")) {
				propertySources.add((MapPropertySource) it);
			}
		});

		propertySources.stream().map(propertySource -> propertySource.getSource().keySet()).flatMap(Collection::stream)
				.distinct().sorted().forEach(key -> {
					try {
						log.info(key + "=" + env.getProperty(key));
					} catch (Exception e) {
						log.warn("{} -> {}", key, e.getMessage());
					}
				});
		log.info("******************************************************************************");
	}
}
