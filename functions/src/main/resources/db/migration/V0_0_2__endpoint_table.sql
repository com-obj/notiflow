
CREATE TABLE nc_endpoint
(
  endpoint_name varchar(100) NOT NULL, 
  endpoint_type varchar(20) NOT NULL,
  recipient_id UUID,
  CONSTRAINT con_pk_endpoint_name PRIMARY KEY(endpoint_name)
);


CREATE TABLE nc_endpoint_processing
(
	endpoint_id UUID,
	processing_id UUID
);

