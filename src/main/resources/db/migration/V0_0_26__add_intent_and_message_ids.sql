alter table nc_intent add previous_intent_ids uuid[];

alter table nc_message add previous_intent_ids uuid[];
alter table nc_message add previous_message_ids uuid[];

alter table nc_processing_info add intent_ids uuid[];
alter table nc_processing_info add message_ids uuid[];

alter table nc_delivery_info add intent_id uuid;