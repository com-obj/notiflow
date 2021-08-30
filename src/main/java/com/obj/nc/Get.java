package com.obj.nc;

import javax.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.obj.nc.repositories.EndpointsRepository;

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