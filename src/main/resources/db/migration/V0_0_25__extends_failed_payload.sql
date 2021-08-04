ALTER TABLE nc_failed_payload ALTER COLUMN error_message TYPE text USING exception_name::text;







