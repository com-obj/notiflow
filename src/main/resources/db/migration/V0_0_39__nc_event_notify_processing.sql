alter table nc_event add column notify_after_processing BOOLEAN DEFAULT FALSE;
CREATE INDEX ON nc_event( notify_after_processing ) WHERE notify_after_processing = true;
