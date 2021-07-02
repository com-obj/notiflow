ALTER TABLE nc_failed_payload ALTER COLUMN exception_name TYPE varchar(1000) USING exception_name::varchar;
ALTER TABLE nc_failed_payload ALTER COLUMN error_message TYPE varchar(1000) USING error_message::varchar;
ALTER TABLE nc_failed_payload ALTER COLUMN root_cause_exception_name TYPE varchar(1000) USING root_cause_exception_name::varchar;