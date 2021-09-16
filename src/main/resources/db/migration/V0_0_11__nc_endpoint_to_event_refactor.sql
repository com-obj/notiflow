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

DROP table nc_endpoint_processing;

CREATE TABLE nc_delivery_info (
	endpoint_id varchar(100) NULL,
	status varchar NULL,
	dalivered_on timestamptz NULL,
	id uuid NOT NULL,
	event_id uuid NULL,
	CONSTRAINT nc_event_2_endpoint_delivery_pk PRIMARY KEY (id)
);

ALTER TABLE nc_delivery_info ADD "version" int NULL;

ALTER TABLE nc_delivery_info RENAME COLUMN dalivered_on TO processed_on;

ALTER TABLE nc_endpoint RENAME COLUMN endpoint_name TO endpoint_id;