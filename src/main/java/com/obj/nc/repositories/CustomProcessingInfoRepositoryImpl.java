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

package com.obj.nc.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.obj.nc.domain.headers.ProcessingInfo;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomProcessingInfoRepositoryImpl implements CustomProcessingInfoRepository {
	
	@Autowired private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate; 
	
	public List<ProcessingInfo> findByAnyEventIdAndStepName(UUID forEventId, String forStep) {
        List<ProcessingInfo> persistedPIs = jdbcTemplate.query(
        		"select * from nc_processing_info "
        		+ "where "
        			+ "'" +forEventId+ "' = ANY (event_ids) " 
        			+ "and step_name='" +forStep+ "'",
        		(rs, rowNum) -> {
	        		 return ProcessingInfo.builder()
	        				.processingId(UUID.fromString(rs.getString("processing_id")))
	        				.prevProcessingId(
	        						rs.getString("prev_processing_id") == null?
	        							null
	        						:
	        							UUID.fromString(rs.getString("prev_processing_id")))
	        				.stepName(rs.getString("step_name"))
	        				.stepIndex(rs.getInt("step_index"))
	        				.timeProcessingStart(rs.getTimestamp("time_processing_start").toInstant())
	        				.timeProcessingEnd(rs.getTimestamp("time_processing_end").toInstant())
	        				.stepDurationMs(rs.getLong("step_duration_ms"))
	        				.payloadJsonStart(rs.getString("payload_json_start"))
	        				.payloadJsonEnd(rs.getString("payload_json_end"))
	        				.eventIds((UUID[])rs.getArray("event_ids").getArray())
	        				.build();
        		}
        	);
        
        return persistedPIs;
	}

}
