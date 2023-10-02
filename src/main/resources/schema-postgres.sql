CREATE TABLE IF NOT EXISTS query (
datasource varchar(256),
engine varchar(256),
query_id varchar(256),
fetch_result_time_string varchar(256),
query_string text,
userid varchar(256),
status varchar(256),
elapsed_time_millis integer,
result_file_size bigint,
linenumber integer,
primary key(datasource, engine, query_id))
;

CREATE TABLE IF NOT EXISTS publish (
publish_id varchar(256),
datasource varchar(256),
engine varchar(256),
query_id varchar(256),
userid varchar(256),
viewers text,
primary key(publish_id))
;

CREATE TABLE IF NOT EXISTS bookmark (
bookmark_id serial primary key,
datasource varchar(256),
engine varchar(256),
query text,
title varchar(256),
userid varchar(256),
snippet varchar(256))
;

CREATE TABLE IF NOT EXISTS comment (
datasource varchar(256),
engine varchar(256),
query_id varchar(256),
content text,
update_time_string varchar(256),
userid varchar(256),
like_count integer,
primary key(datasource, engine, query_id))
;

CREATE TABLE IF NOT EXISTS starred_schema (
starred_schema_id serial primary key,
datasource varchar(256) not null,
engine varchar(256) not null,
catalog varchar(256) not null,
schema varchar(256) not null,
userid varchar(256))
;

CREATE TABLE IF NOT EXISTS session_property (
session_property_id serial primary key,
datasource varchar(256) not null,
engine varchar(256) not null,
query_id varchar(256) not null,
session_key varchar(256) not null,
session_value varchar(256) not null)
;
