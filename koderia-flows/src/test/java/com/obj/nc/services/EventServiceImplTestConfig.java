package com.obj.nc.services;

import com.obj.nc.mapper.EventMapperImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import({
        EventServiceImpl.class,
        EventMapperImpl.class
})
public class EventServiceImplTestConfig {
}
