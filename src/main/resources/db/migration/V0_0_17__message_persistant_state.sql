DELETE FROM nc_message;
ALTER TABLE nc_message ADD message_class varchar NOT NULL;
ALTER TABLE nc_message RENAME COLUMN payload_json TO content_json;

