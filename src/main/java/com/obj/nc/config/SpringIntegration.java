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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableMessageHistory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.json.JacksonJsonUtils;

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
