/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


ALTER TABLE nc_processing_info rename column event_json to payload_json_start;
ALTER TABLE nc_processing_info rename column event_json_diff to payload_json_end;

ALTER TABLE nc_processing_info DROP COLUMN event_ids;	
ALTER TABLE nc_processing_info add COLUMN event_ids uuid[];	

alter table nc_processing_info add column time_created timestamptz;

alter table nc_processing_info drop column payload_id;

alter table nc_processing_info drop column payload_json_start;
alter table nc_processing_info drop column payload_json_end;

alter table nc_processing_info add column payload_json_start TEXT;
alter table nc_processing_info add column payload_json_end TEXT;

CREATE INDEX nc_processing_info_event_ids_idx ON nc_processing_info (event_ids);

alter table nc_processing_info add column version int;
