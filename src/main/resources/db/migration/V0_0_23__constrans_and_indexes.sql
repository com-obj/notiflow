ALTER TABLE nc_delivery_info ADD CONSTRAINT nc_delivery_info_event_fk FOREIGN KEY (event_id) REFERENCES nc_event(id);
ALTER TABLE nc_delivery_info ADD CONSTRAINT nc_delivery_info_message_fk FOREIGN KEY (message_id) REFERENCES nc_message(id);
ALTER TABLE nc_delivery_info ADD CONSTRAINT nc_delivery_info_enpoint_fk FOREIGN KEY (endpoint_id) REFERENCES nc_endpoint(id);
ALTER TABLE nc_delivery_info ADD CONSTRAINT nc_delivery_info_failed_payload_fk FOREIGN KEY (failed_payload_id) REFERENCES nc_failed_payload(id);


CREATE INDEX nc_delivery_info_event_id_idx ON nc_delivery_info (event_id);
CREATE INDEX nc_delivery_info_message_id_idx ON nc_delivery_info (message_id);
CREATE INDEX nc_delivery_info_endpoint_id_idx ON nc_delivery_info (endpoint_id);
ALTER INDEX nc_event_2_endpoint_delivery_pk RENAME TO nc_delivery_info_pk;

ALTER INDEX nc_endpoint_endpoint_id_unique_key RENAME TO nc_endpoint_name_unique_key;

CREATE INDEX nc_intent_event_ids_idx ON nc_intent USING GIN (event_ids);

CREATE INDEX nc_message_event_ids_idx ON nc_message USING GIN (event_ids);
CREATE INDEX nc_message_endpoint_ids_idx ON nc_message USING GIN (endpoint_ids);

ALTER TABLE nc_processing_info ADD CONSTRAINT nc_processing_info_fk FOREIGN KEY (prev_processing_id) REFERENCES nc_processing_info(processing_id);








