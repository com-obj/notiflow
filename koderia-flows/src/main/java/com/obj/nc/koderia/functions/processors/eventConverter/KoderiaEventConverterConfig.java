package com.obj.nc.koderia.functions.processors.eventConverter;

import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.koderia.domain.event.*;
import com.obj.nc.utils.JsonUtils;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class KoderiaEventConverterConfig {
    @PostConstruct
    public void registerJsonMixins() {
        JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, BaseKoderiaEvent.class);
    }
}
