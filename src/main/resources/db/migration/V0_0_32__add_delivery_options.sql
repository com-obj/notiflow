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

CREATE TABLE nc_delivery_options (
    id UUID NOT NULL DEFAULT uuid_in(md5(random()::text || clock_timestamp()::text)::cstring),
    time_created timestamptz NOT NULL,
    message_id UUID NOT NULL,
    endpoint_id UUID NOT NULL,
    delivery_options jsonb,
    CONSTRAINT fk_message
        FOREIGN KEY(message_id) REFERENCES nc_message(id),
    CONSTRAINT fk_endpoint
        FOREIGN KEY(endpoint_id) REFERENCES nc_endpoint(id)
);
ALTER TABLE nc_delivery_options ALTER COLUMN time_created SET DEFAULT now();

INSERT INTO nc_delivery_options (message_id, endpoint_id)
SELECT id as message_id, UNNEST(endpoint_ids) as endpoint_id
FROM nc_message;

ALTER TABLE nc_message DROP COLUMN endpoint_ids;