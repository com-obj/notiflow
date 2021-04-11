DROP INDEX ndx_endpoint_processing;

ALTER TABLE nc_endpoint_processing RENAME TO nc_event_2_endpoint_delivery;
ALTER TABLE nc_event_2_endpoint_delivery DROP COLUMN processing_id;

ALTER TABLE nc_event_2_endpoint_delivery ADD event_ids UUID[];
ALTER TABLE nc_event_2_endpoint_delivery ADD status varchar[];
ALTER TABLE nc_event_2_endpoint_delivery ADD dalivered_on timestamp with time ZONE;

alter table nc_event_2_endpoint_delivery add id UUID;
ALTER TABLE nc_event_2_endpoint_delivery ADD CONSTRAINT nc_event_2_endpoint_delivery_pk PRIMARY KEY (id);

alter table nc_input 
add COLUMN time_created timestamptz;