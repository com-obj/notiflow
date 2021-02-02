package com.obj.nc.functions.sources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.obj.nc.domain.event.Event;
import com.obj.nc.domain.message.Message;
import com.obj.nc.utils.JsonUtils;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

@EnableScheduling
@Configuration
@RequiredArgsConstructor
@Log4j2
@Profile("dev")
public class EventGenerator {
	
	@Autowired
	@NotNull
	private EventSourceConfig eventSourceConfig; 
	
	private final EmitterProcessor<Event> streamSource = EmitterProcessor.create();
	
//REACTIVE START
	@Bean
    public Supplier<Flux<Event>> generateEvent() {
		return () -> streamSource;
	}
	
    @Scheduled(fixedDelay = 1000)
	public void generateEventAndAddToFlux() {
    	Event event =  readEventsFromFile(); 
    	if (event == null) {
    		return;
    	}
		
    	streamSource.onNext(event);
	}
//REACTIVE END

	private int eventFileIndex = 0;
	
	public Event readEventsFromFile()  {
		try {
			Path eventFile = null;
			
			if (eventSourceConfig.getFileName()==null) {
				List<Path> eventFiles = Files.list(Paths.get(eventSourceConfig.getSourceDir()))
						.filter(path -> path.toString().endsWith(".json"))
						.collect(Collectors.toList());
				
				if (eventFiles.size()==0) {
					return null;
				}
				
				eventFiles.sort(Comparator.comparing(eventFilePath -> eventFilePath.toAbsolutePath().toString()));
				
				eventFile = eventFiles.get(eventFileIndex++);
				if (eventFileIndex>=eventFiles.size()) {
					eventFileIndex = 0;
				}
			} else {
				eventFile = Paths.get(eventSourceConfig.getSourceDir(), eventSourceConfig.getFileName());
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

	@Deprecated(since = "FOR TESTS ONLY")
	public void setEventSourceConfig(EventSourceConfig eventSourceConfig) {
		this.eventSourceConfig = eventSourceConfig;
	}

	@ConfigurationProperties(prefix = "nc.functions.event-generator")
	@Data
	@Component
	public static class EventSourceConfig {
		
		String sourceDir;
		//if null, reader rotates in directory
		String fileName;
	
	}


}
