
CREATE TABLE nc_endpoint
(
  endpoint_name varchar(100) NOT NULL, 
  endpoint_type varchar(20) NOT NULL,
  recipient_id UUID,
  CONSTRAINT con_pk_endpoint_name PRIMARY KEY(endpoint_name)
);


CREATE TABLE nc_endpoint_processing
(
	endpoint_id varchar(100),
	processing_id UUID
);

CREATE INDEX ndx_endpoint_processing on nc_endpoint_processing (endpoint_id, processing_id);
