package com.obj.nc.controllers;

import com.obj.nc.domain.event.GenericEventStats;
import com.obj.nc.repositories.GenericEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		return eventsRepository.findEventStatsByEventId(UUID.fromString(eventId)).getStats();
	}
	
}
