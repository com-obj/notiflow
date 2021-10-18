/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.testUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;
import org.springframework.util.StringUtils;

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
        if (resolvedProfiles.length == 0) {
            resolvedProfiles = new String[] { "test" };
        }

        if (System.getProperties().containsKey(springProfileKey) && StringUtils.hasText(System.getProperty(springProfileKey))) {
            resolvedProfiles = Stream.of(resolvedProfiles, System.getProperty(springProfileKey).split("\\s*,\\s*"))
                    .flatMap(Stream::of)
                    .toArray(String[]::new);
        }

        return resolvedProfiles;
    }

}
