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

package com.obj.nc;

import com.obj.nc.repositories.EndpointsRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * This is @Component to give spring a hint in configuration ordering. 
 * If using this class might cause issues when using with tests. In test, spring can re-initialise the ApplicationContext and invalidates the old one. 
 * Although this class uses application listeners to be notified about the new ApplicationContext, it doesn't work reliably. Tests needs to make sure to set the new 
 * ApplicationContext to this class. The simplest way to do this is to extend the test class from BaseIntegrationTest.
 * @author Jan Cuzy
 *
 */
@Component
public class Get {

    private static Get instance;

    private ApplicationContext applicationContext;

    @PostConstruct
    public void registerInstance() {
        instance = this;
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }
    
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return getApplicationContext().getBean(beanName, clazz);
    }
    
    public static JdbcTemplate getJdbc() {
    	return getBean(JdbcTemplate.class);
    }
    
    public static EndpointsRepository getEndpointsRepo() {
    	return getBean(EndpointsRepository.class);
    }
    
	public static ApplicationContext getApplicationContext() {
		return instance.applicationContext;
	}
	
	public static boolean isInitalised() {
		return getApplicationContext() != null;
	}
	
	public void registerBean(String beanName, Class<?> beanClass) {
		if (applicationContext instanceof GenericApplicationContext) {
			((GenericApplicationContext) applicationContext)
					.registerBean(beanName, beanClass);
		}
	}
	
	//we need all of them.. in tests
	@EventListener
	public void refreshApplicationContext(ContextRefreshedEvent refresEvent) {
		instance.applicationContext = refresEvent.getApplicationContext();
	}
	
	@EventListener
	public void refreshApplicationContext(ContextStartedEvent startEvent) {
		instance.applicationContext = startEvent.getApplicationContext();
	}
	
	public static void setApplicationContext(ApplicationContext applicationContext) {
		instance.applicationContext = applicationContext;
	}
	
	


}