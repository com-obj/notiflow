alter table nc_endpoint rename column recipient_id to id;
alter table nc_endpoint alter column id set not null;
alter table nc_endpoint drop constraint con_pk_endpoint_name;
alter table nc_endpoint add constraint nc_endpoint_pkey primary key (id);
