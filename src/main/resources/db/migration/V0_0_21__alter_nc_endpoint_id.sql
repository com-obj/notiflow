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

alter table nc_endpoint drop constraint con_pk_endpoint_name;
alter table nc_endpoint rename column endpoint_id to endpoint_name;

alter table nc_endpoint add column id uuid not null default uuid_in(md5(random()::text || clock_timestamp()::text)::cstring);
alter table nc_endpoint add constraint nc_endpoint_pkey primary key (id);
alter table nc_endpoint add constraint nc_endpoint_endpoint_id_unique_key unique (endpoint_name);

alter table nc_message rename column endpoint_ids to endpoint_names;
alter table nc_message add column endpoint_ids uuid[];
update nc_message as msg set endpoint_ids = array(select ep.id from nc_endpoint ep where ep.endpoint_name = any(msg.endpoint_names)) where msg.endpoint_ids is null;
alter table nc_message drop column endpoint_names;

alter table nc_delivery_info rename column endpoint_id to endpoint_name;
alter table nc_delivery_info add column endpoint_id uuid;
update nc_delivery_info as di set endpoint_id = ep.id from nc_endpoint ep where di.endpoint_name = ep.endpoint_name and di.endpoint_id is null;
alter table nc_delivery_info drop column endpoint_name;
