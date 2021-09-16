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
