package com.obj.nc.controller;

import com.obj.nc.mapper.EventMapperImpl;
import com.obj.nc.services.EventServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        EventServiceImpl.class,
        EventMapperImpl.class
})

public class CreateEventsControllerTestConfig {
}
