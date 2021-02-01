package com.obj.nc.functions.sources;

import com.obj.nc.domain.event.Event;
import com.obj.nc.exceptions.PayloadValidationException;
import com.obj.nc.functions.PreCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Log4j2
@Profile("dev")
public class EventGeneratorPreCondition implements PreCondition<Event> {

    @Autowired
    private EventGeneratorConfig eventGeneratorConfig;

    private int eventFileIndex = 0;

    @Override
    public Optional<PayloadValidationException> apply(Event event) {
        Path eventFile = null;

        if (eventGeneratorConfig.getFileName()==null) {
            List<Path> eventFiles = null;

            try {
                eventFiles = Files.list(Paths.get(eventGeneratorConfig.getSourceDir()))
                        .filter(path -> path.toString().endsWith(".json"))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                return Optional.of(new PayloadValidationException(e.getMessage()));
            }

            if (eventFiles.size()==0) {
                return Optional.of(new PayloadValidationException("No file found"));
            }

            eventFiles.sort(Comparator.comparing(eventFilePath -> eventFilePath.toAbsolutePath().toString()));

            eventFile = eventFiles.get(eventFileIndex++);
            if (eventFileIndex>=eventFiles.size()) {
                eventFileIndex = 0;
            }
        } else {
            eventFile = Paths.get(eventGeneratorConfig.getSourceDir(), eventGeneratorConfig.getFileName());

            if (!Files.exists(eventFile)) {
                String errorMessage = "Configuration is referencing file, which doesn't exists %s";
                log.error(errorMessage, eventFile);
                return Optional.of(new PayloadValidationException(String.format(errorMessage, eventFile)));
            }
        }

        return Optional.empty();
    }

}
