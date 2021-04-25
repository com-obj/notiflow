CREATE TABLE nc_intent (
	id uuid NOT NULL,
	flow_id varchar(100),
	event_ids _uuid NULL,
	payload_json jsonb NOT NULL,
	time_created timestamptz NULL,
	CONSTRAINT nc_intent_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ndx_intent_id ON nc_intent USING btree (id);