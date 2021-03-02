package com.obj.nc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

//Spring by default neumoznuje overidnut @ActiveProfiles annotaciu pomocou -D parametra,.. toto je trik ako sa to da:
//https://blog.inspeerity.com/spring/setting-default-spring-profile-for-tests-with-override-option/
public class SystemPropertyActiveProfileResolver implements ActiveProfilesResolver {

    private static final Logger log = LoggerFactory.getLogger(SystemPropertyActiveProfileResolver.class);

    private final DefaultActiveProfilesResolver defaultActiveProfilesResolver = new DefaultActiveProfilesResolver();

    @Override
    public String[] resolve(Class<?> testClass) {
        log.info("Searching for active profiles setting in program parameters: " + System.getProperties());

        final String springProfileKey = "spring.profiles.active";


        return System.getProperties().containsKey(springProfileKey)
                ? System.getProperty(springProfileKey).split("\\s*,\\s*")
                : defaultActiveProfilesResolver.resolve(testClass);
    }

}