ALTER TABLE nc_processing_info RENAME COLUMN event_json_diff TO payload_json_diff;
ALTER TABLE nc_processing_info RENAME COLUMN event_json TO payload_json;