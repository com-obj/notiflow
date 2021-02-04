ALTER TABLE nc_processing_info
    DROP COLUMN event_id,
    ADD COLUMN event_ids json NOT NULL;