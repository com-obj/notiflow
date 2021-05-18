package com.obj.nc.koderia.config;

import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.koderia.domain.event.BaseKoderiaEvent;
import com.obj.nc.utils.JsonUtils;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class DomainConfig {
    @PostConstruct
    public void registerJsonMixins() {
        JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, BaseKoderiaEvent.class);
    }
}
