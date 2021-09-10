package com.obj.nc.controllers;

import com.obj.nc.domain.event.GenericEventStats;
import com.obj.nc.domain.event.GenericEventWithStats;
import com.obj.nc.repositories.GenericEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Validated
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsRestController {
	
	private final GenericEventRepository eventsRepository;
	
	@GetMapping(value = "/events/{eventId}", produces = APPLICATION_JSON_VALUE)
	public GenericEventStats findEventStats(@PathVariable("eventId") String eventId) {
		return eventsRepository
				.findAllEventsWithStats(
						Instant.parse("2000-01-01T12:00:00Z"), 
						Instant.parse("9999-01-01T12:00:00Z"), 
						UUID.fromString(eventId),
						0, 1)
				.stream()
				.findFirst()
				.map(GenericEventWithStats::getStats)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}
	
}
