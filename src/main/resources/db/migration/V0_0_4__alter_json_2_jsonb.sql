ALTER TABLE nc_processing_info
  ALTER COLUMN event_ids
  SET DATA TYPE jsonb
  USING event_ids::jsonb;
 
ALTER TABLE nc_processing_info
  ALTER COLUMN event_json
  SET DATA TYPE jsonb
  USING event_json::jsonb;
  
ALTER TABLE nc_processing_info
  ALTER COLUMN event_json_diff
  SET DATA TYPE jsonb
  USING event_json_diff::jsonb;
  