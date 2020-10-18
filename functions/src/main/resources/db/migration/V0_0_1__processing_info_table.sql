CREATE TABLE nc_processing_info
(
  processing_id UUID NOT NULL PRIMARY KEY, 
  prev_processing_id UUID,
  payload_id UUID NOT null, 
  payload_type varchar(20),
  step_name varchar(100),
  step_index smallint,
  time_processing_start timestamp with time ZONE,
  time_processing_end timestamp with time ZONE,
  step_duration_ms bigint,
  event_json json NOT NULL,
  event_json_diff json
)

