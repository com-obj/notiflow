CREATE TABLE nc_input (
	payload_id UUID not null, 
	flow_id varchar(100) NOT NULL,
	external_id varchar(100),
	payload_json jsonb NOT null,
	CONSTRAINT nc_input_pkey PRIMARY KEY (payload_id)
);