package com.obj.nc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "postgres")
public class PostgresConfigurationProperties {

    private String dockerImage;

    private String user;

    private String password;

    private String databaseName;

    private int hostPort;

}
