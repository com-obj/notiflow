package com.obj.nc;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
/**
 * This is spring component to give spring a hint in configuration ordering. As component it should be staticaly usable because that
 * way clients can run into problems with ordering
 * @author ja
 *
 */
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
	public static ApplicationContext getApplicationContext() {
		return instance.applicationContext;
	}
	
	public static boolean isInitalised() {
		return getApplicationContext() != null;
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