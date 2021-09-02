ALTER INDEX nc_intent_event_ids_idx RENAME TO nc_intent_previous_event_ids_idx;
CREATE INDEX nc_intent_previous_intent_ids_idx ON nc_intent USING GIN (previous_intent_ids);

ALTER INDEX nc_message_event_ids_idx RENAME TO nc_message_previous_event_ids_idx;
CREATE INDEX nc_message_previous_intent_ids_idx ON nc_message USING GIN (previous_intent_ids);
CREATE INDEX nc_message_previous_message_ids_idx ON nc_message USING GIN (previous_message_ids);

CREATE INDEX nc_delivery_info_intent_id_idx ON nc_delivery_info (intent_id);