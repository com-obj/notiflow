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


CREATE TABLE nc_endpoint
(
  endpoint_name varchar(100) NOT NULL, 
  endpoint_type varchar(20) NOT NULL,
  recipient_id UUID,
  CONSTRAINT con_pk_endpoint_name PRIMARY KEY(endpoint_name)
);


CREATE TABLE nc_endpoint_processing
(
	endpoint_id varchar(100),
	processing_id UUID
);

CREATE INDEX ndx_endpoint_processing on nc_endpoint_processing (endpoint_id, processing_id);
