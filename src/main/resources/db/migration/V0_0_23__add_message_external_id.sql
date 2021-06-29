alter table nc_message add column external_id varchar(100);
create unique index ndx_message_external_id ON nc_message (external_id);
