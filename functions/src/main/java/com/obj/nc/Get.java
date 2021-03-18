package com.obj.nc;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class Get {

    private static Get instance;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void registerInstance() {
        instance = this;
    }

    public static <T> T getBean(Class<T> clazz) {
        return instance.applicationContext.getBean(clazz);
    }
    
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return instance.applicationContext.getBean(beanName, clazz);
    }
    
    public static JdbcTemplate getJdbc() {
    	return getBean(JdbcTemplate.class);
    }

}