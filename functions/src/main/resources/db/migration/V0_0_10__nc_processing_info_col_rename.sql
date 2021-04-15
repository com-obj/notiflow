
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
