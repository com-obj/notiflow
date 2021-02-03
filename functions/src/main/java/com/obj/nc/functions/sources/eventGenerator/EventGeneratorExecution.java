package com.obj.nc.functions.sources.eventGenerator;

import com.obj.nc.domain.event.Event;
import com.obj.nc.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@EnableScheduling
@Component
@RequiredArgsConstructor
@Log4j2
@Profile("dev")
public class EventGeneratorExecution implements Supplier<Event> {

    @Autowired
    @NotNull
    private EventGeneratorConfig eventGeneratorConfig;

    @Override
    public Event get() {
        return readEventsFromFile();
    }

    private int eventFileIndex = 0;

    public Event readEventsFromFile()  {
        try {
            Path eventFile = null;

            if (eventGeneratorConfig.getFileName()==null) {
                List<Path> eventFiles = Files.list(Paths.get(eventGeneratorConfig.getSourceDir()))
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
                eventFile = Paths.get(eventGeneratorConfig.getSourceDir(), eventGeneratorConfig.getFileName());
                if (!Files.exists(eventFile)) {
                    log.error("Configuration is referencing file, which doesn't exists {}", eventFile);
                    return null;
                }
            }

            Event event = JsonUtils.readObjectFromJSONFile(eventFile,Event.class);

            Files.delete(eventFile);

            return event;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
