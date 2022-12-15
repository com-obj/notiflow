-- activate uuid extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE nc_osk_message_reference
(
    id uuid default uuid_generate_v4() primary key,
    message_id uuid,
    reference_number varchar(255),
    FOREIGN KEY (message_id) REFERENCES nc_message(id)
);