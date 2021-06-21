CREATE TABLE nc_message (
	id uuid NOT NULL,
	flow_id varchar(100),
	event_ids _uuid NULL,
	payload_json jsonb NOT NULL,
	time_created timestamptz NULL,
	CONSTRAINT nc_message_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ndx_message_id ON nc_message USING btree (id);

-- TODO: CHECK, copy of nc_intent