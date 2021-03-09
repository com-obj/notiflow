package com.obj.nc;

import com.obj.nc.config.PostgresConfigurationProperties;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@ConditionalOnProperty(value = "testContainers", havingValue = "jenkins")
public class JenkinsTestContainerConfigurer {

    @Autowired
    private PostgresConfigurationProperties properties;

    @PostConstruct
    public void initContainer() {
        JenkinsTestContainers.initContainers(properties.getDockerImage(),
                properties.getUser(),
                properties.getPassword(),
                properties.getDatabaseName(),
                properties.getHostPort());

        Flyway flyway = JenkinsTestContainers.getFlyway();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        flyway.migrate();
    }

}
