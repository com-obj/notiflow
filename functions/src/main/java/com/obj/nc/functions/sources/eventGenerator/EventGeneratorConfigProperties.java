package com.obj.nc.functions.sources.eventGenerator;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nc.functions.event-generator")
@Data
@Component
public class EventGeneratorConfigProperties {

    String sourceDir;
    //if null, reader rotates in directory
    String fileName;

}
