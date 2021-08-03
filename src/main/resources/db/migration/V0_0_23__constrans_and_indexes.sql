CREATE INDEX nc_delivery_info_event_id_idx ON nc_delivery_info (event_id);
CREATE INDEX nc_delivery_info_message_id_idx ON nc_delivery_info (message_id);
CREATE INDEX nc_delivery_info_endpoint_id_idx ON nc_delivery_info (endpoint_id);
ALTER INDEX nc_event_2_endpoint_delivery_pk RENAME TO nc_delivery_info_pk;

ALTER INDEX nc_endpoint_endpoint_id_unique_key RENAME TO nc_endpoint_name_unique_key;

CREATE INDEX nc_intent_event_ids_idx ON nc_intent USING GIN (event_ids);

CREATE INDEX nc_message_event_ids_idx ON nc_message USING GIN (event_ids);
CREATE INDEX nc_message_endpoint_ids_idx ON nc_message USING GIN (endpoint_ids);









