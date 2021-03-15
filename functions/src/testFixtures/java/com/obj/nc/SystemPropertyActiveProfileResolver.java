package com.obj.nc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;

//Spring by default neumoznuje overidnut @ActiveProfiles annotaciu pomocou -D parametra,.. toto je trik ako sa to da:
//https://blog.inspeerity.com/spring/setting-default-spring-profile-for-tests-with-override-option/
public class SystemPropertyActiveProfileResolver implements ActiveProfilesResolver {

    private static final Logger log = LoggerFactory.getLogger(SystemPropertyActiveProfileResolver.class);

    private final DefaultActiveProfilesResolver defaultActiveProfilesResolver = new DefaultActiveProfilesResolver();

    @Override
    public String[] resolve(Class<?> testClass) {
        log.info("Searching for active profiles setting in program parameters: " + System.getProperties());

        final String springProfileKey = "spring.profiles.active";

        String[] resolvedProfiles = defaultActiveProfilesResolver.resolve(testClass);

        if (System.getProperties().containsKey(springProfileKey) && StringUtils.hasText(System.getProperty(springProfileKey))) {
            resolvedProfiles = Stream.of(resolvedProfiles, System.getProperty(springProfileKey).split("\\s*,\\s*"))
                    .flatMap(Stream::of)
                    .toArray(String[]::new);
        }

        return resolvedProfiles;
    }

}
