package com.obj.nc.koderia.config;

import com.obj.nc.domain.IsTypedJson;
import com.obj.nc.koderia.dto.koderia.event.BaseKoderiaEventDto;
import com.obj.nc.utils.JsonUtils;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class NotifEventConverterConfig {
    @PostConstruct
    public void registerJsonMixins() {
        JsonUtils.getObjectMapper().addMixIn(IsTypedJson.class, BaseKoderiaEventDto.class);
    }
}
