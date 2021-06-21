alter table nc_endpoint drop constraint con_pk_endpoint_name;
alter table nc_endpoint rename column endpoint_id to endpoint_name;

create extension if not exists pgcrypto;

alter table nc_endpoint add column id uuid not null default gen_random_uuid();
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
