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

package com.obj.nc.functions.sources.eventGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.notifIntent.NotificationIntent;
import com.obj.nc.utils.JsonUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class EventGeneratorExecution implements Supplier<NotificationIntent> {

    @Autowired
    @NotNull
    private EventGeneratorConfigProperties eventGeneratorConfigProperties;

    @Override
    public NotificationIntent get() {
        return readNotificationIntentsFromFile();
    }

    private int eventFileIndex = 0;

    public NotificationIntent readNotificationIntentsFromFile()  {
        try {
            Path eventFile = null;

            if (eventGeneratorConfigProperties.getFileName()==null) {
                List<Path> eventFiles = Files.list(Paths.get(eventGeneratorConfigProperties.getSourceDir()))
                        .filter(path -> path.toString().endsWith(".json"))
                        .collect(Collectors.toList());

                if (eventFiles.size()==0) {
                    return null;
                }

                eventFiles.sort(Comparator.comparing(eventFilePath -> eventFilePath.toAbsolutePath().toString()));

                if (eventFileIndex>=eventFiles.size()) {
                    eventFileIndex = 0;
                }
                eventFile = eventFiles.get(eventFileIndex++);

            } else {
                eventFile = Paths.get(eventGeneratorConfigProperties.getSourceDir(), eventGeneratorConfigProperties.getFileName());
                if (!Files.exists(eventFile)) {
                    log.error("Configuration is referencing file, which doesn't exists {}", eventFile);
                    return null;
                }
            }

            NotificationIntent notificationIntent = JsonUtils.readObjectFromJSONFile(eventFile,NotificationIntent.class);

            Files.delete(eventFile);

            return notificationIntent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
