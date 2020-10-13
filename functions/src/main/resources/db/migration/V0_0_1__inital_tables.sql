CREATE TABLE nc_event_processing
(
  event_id UUID NOT null, 
  processing_id UUID NOT NULL PRIMARY KEY, 
  prev_processing_id UUID,
  step_name varchar(100),
  step_index smallint,
  time_processing_start timestamp with time ZONE,
  time_processing_end timestamp with time ZONE,
  step_duration_ms bigint,
  event_json json NOT NULL,
  event_json_diff json
)

