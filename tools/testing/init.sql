select 1;

CREATE ROLE folio_admin LOGIN PASSWORD 'folio_admin' SUPERUSER CREATEDB CREATEROLE;
CREATE ROLE crosslink LOGIN PASSWORD 'crosslink';
CREATE ROLE crosslink_broker NOLOGIN;

DROP DATABASE IF EXISTS okapi_modules;
CREATE DATABASE okapi_modules OWNER folio_admin;
GRANT ALL PRIVILEGES ON DATABASE okapi_modules TO folio_admin;

-- BROKER SETUP
\connect okapi_modules
GRANT crosslink_broker TO crosslink;
GRANT CONNECT ON DATABASE okapi_modules TO crosslink;

CREATE SCHEMA IF NOT EXISTS crosslink_broker AUTHORIZATION crosslink_broker;

REVOKE ALL ON SCHEMA public FROM crosslink;
GRANT USAGE, CREATE ON SCHEMA crosslink_broker TO crosslink;

ALTER ROLE crosslink IN DATABASE okapi_modules
SET search_path = crosslink_broker, pg_temp;
-- END BROKER SETUP

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;
