package com.obj.nc.testmode.functions.sources;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Data
@Configuration
@ConditionalOnProperty(value = "testmode.enabled", havingValue = "true")
public class GreenMailReceiverConfig {

    @Autowired
    private Environment environment;

    private GreenMail greenMail;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (greenMail != null) {
            return;
        }

        greenMail = new GreenMail(ServerSetupTest.ALL);
        greenMail.setUser(environment.getProperty("spring.mail.username"),
                environment.getProperty("spring.mail.password"));
        greenMail.start();
    }

    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) {
        greenMail.stop();
        greenMail = null;
    }

}
