DROP table nc_endpoint_processing;

CREATE TABLE nc_delivery_info (
	endpoint_id varchar(100) NULL,
	status varchar NULL,
	dalivered_on timestamptz NULL,
	id uuid NOT NULL,
	event_id uuid NULL,
	CONSTRAINT nc_event_2_endpoint_delivery_pk PRIMARY KEY (id)
);

ALTER TABLE nc_delivery_info ADD "version" int NULL;

ALTER TABLE nc_delivery_info RENAME COLUMN dalivered_on TO processed_on;

ALTER TABLE nc_endpoint RENAME COLUMN endpoint_name TO endpoint_id;