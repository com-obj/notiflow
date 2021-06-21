package com.obj.nc.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.json.JacksonJsonUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@EnableMessageHistory
@Configuration
public class SpringIntegration  {
	
	public static final String OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME = "jsonConverterForSpringMessages";
	
	@Bean
	public MessagingTemplate messagingTemplate(ApplicationContext beanFactory) {
		MessagingTemplate tmpl = new MessagingTemplate();
		tmpl.setBeanFactory(beanFactory);
		return tmpl;
	}
	
	@Bean(OBJECT_MAPPER_FOR_SPRING_MESSAGES_BEAN_NAME)
	public ObjectMapper jsonConverterForSpringMessages() {
		return JacksonJsonUtils.messagingAwareMapper("com.obj.nc", "org.springframework.integration", "org.springframework.messaging");
	}
    
}
