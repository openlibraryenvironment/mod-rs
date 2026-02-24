select 1;

CREATE USER folio_admin WITH PASSWORD 'folio_admin';
DROP DATABASE if exists okapi_modules;
CREATE DATABASE okapi_modules;
GRANT ALL PRIVILEGES ON DATABASE okapi_modules to folio_admin;

ALTER USER folio_admin CREATEDB;
ALTER USER folio_admin CREATEROLE;
ALTER USER folio_admin WITH SUPERUSER;

-- BROKER setup
-- create app login role
CREATE ROLE crosslink LOGIN PASSWORD 'crosslink';

-- Create non-login role that owns broker schema/privileges
CREATE ROLE crosslink_broker NOLOGIN;

-- Grant ownership role to app login role
GRANT crosslink_broker TO crosslink;

-- Allow this role to connect to the database
GRANT CONNECT ON DATABASE "okapi_modules" TO crosslink;

-- Create schema owned by ownership role
-- Note: if schema already exists (migration) ensure USAGE and CREATE are granted for future and existing objects
CREATE SCHEMA IF NOT EXISTS crosslink_broker AUTHORIZATION crosslink_broker;

-- Prevent crosslink from using or creating in public schema
REVOKE ALL ON SCHEMA public FROM crosslink;

-- Set default schema for this role
ALTER ROLE crosslink IN DATABASE "okapi_modules"

SET search_path = crosslink_broker, pg_temp;
-- END broker setup

CREATE EXTENSION IF NOT EXISTS pg_trgm;
