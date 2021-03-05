package com.obj.nc.functions.sources.eventGenerator;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "nc.functions.event-generator")
@Data
@Component
@Profile({"dev", "test"})
public class EventGeneratorConfigProperties {

    String sourceDir;
    //if null, reader rotates in directory
    String fileName;

}
