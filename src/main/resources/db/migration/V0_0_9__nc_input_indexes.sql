delete from nc_input;

CREATE UNIQUE INDEX ndx_input_event_external_id ON nc_input (external_id);
CREATE UNIQUE INDEX ndx_input_event_id ON nc_input (id);