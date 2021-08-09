alter table nc_endpoint add column time_created timestamptz;
update nc_endpoint set time_created = now();








