package com.obj.nc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.obj.nc.Get;

@Configuration
public class InjectorConfiguration  {

    @Bean
    public Get get() { 
        return new Get();
    }
    
}
