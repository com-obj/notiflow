package com.obj.nc.functions.sources.eventGenerator;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "nc.functions.event-generator")
@Data
@Component
public class EventGeneratorConfigProperties {

    String sourceDir;
    //if null, reader rotates in directory
    String fileName;

}
