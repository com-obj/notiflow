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

CREATE TABLE nc_processing_info
(
  processing_id UUID NOT NULL PRIMARY KEY, 
  prev_processing_id UUID,
  event_id UUID NOT null, 
  payload_id UUID NOT null, 
  payload_type varchar(20),
  step_name varchar(100),
  step_index smallint,
  time_processing_start timestamp with time ZONE,
  time_processing_end timestamp with time ZONE,
  step_duration_ms bigint,
  event_json json NOT NULL,
  event_json_diff json
)

