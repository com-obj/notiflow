alter table nc_intent add column time_consumed timestamptz null;
alter table nc_intent add column external_id varchar(100);

create unique index ndx_intent_external_id ON nc_intent (external_id);
