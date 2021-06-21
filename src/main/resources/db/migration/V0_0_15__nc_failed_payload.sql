
CREATE TABLE nc_failed_payload (
	id uuid NOT NULL,
	flow_id varchar(100) NOT NULL,
	message_json jsonb NOT NULL,
	exception_name varchar(100),
	error_message varchar(100),
	channel_name_for_retry varchar(100),
	root_cause_exception_name varchar(100),
	stack_trace text,
	time_created timestamptz NULL,
	time_resurected timestamptz NULL,
	CONSTRAINT nc_failed_payload_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ndx_failed_payload_id ON nc_failed_payload USING btree (id);

-- TODO: CHECK, copy of nc_intent