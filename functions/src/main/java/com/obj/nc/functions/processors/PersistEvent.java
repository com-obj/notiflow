package com.obj.nc.functions.processors;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.obj.nc.domain.event.Event;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
@AllArgsConstructor
public class PersistEvent {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	

	@Bean
	public Consumer<Event> persistEvent2() {
		return (event) -> {
			persistEventImpl(event);
		};
	}

	public void persistEventImpl(Event event) {
		log.info("Persisting event {}",event);
		
		String inserEventSQL = "INSERT INTO nc_event (id, processing_id, event_json) VALUES (?, ?, ?)";
		
		jdbcTemplate.update(inserEventSQL,
				event.getId(),
				event.getHeader().getProcessingID(),
				event.toJSONString());
	}
}
