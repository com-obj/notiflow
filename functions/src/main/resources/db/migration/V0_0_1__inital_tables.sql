CREATE TABLE nc_event 
(
  id UUID NOT null PRIMARY KEY, 
  processing_id UUID NOT null, 
  event_json json NOT NULL
)