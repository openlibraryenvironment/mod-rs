select 1;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='folio_admin') THEN
    CREATE ROLE folio_admin LOGIN PASSWORD 'folio_admin' SUPERUSER CREATEDB CREATEROLE;
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='crosslink') THEN
    CREATE ROLE crosslink LOGIN PASSWORD 'crosslink';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='crosslink_broker') THEN
    CREATE ROLE crosslink_broker NOLOGIN;
  END IF;
END $$;

DROP DATABASE IF EXISTS okapi_modules;
CREATE DATABASE okapi_modules OWNER folio_admin;
GRANT ALL PRIVILEGES ON DATABASE okapi_modules TO folio_admin;

-- BROKER setup
\connect okapi_modules
GRANT crosslink_broker TO crosslink;
GRANT CONNECT ON DATABASE okapi_modules TO crosslink;

CREATE SCHEMA IF NOT EXISTS crosslink_broker AUTHORIZATION crosslink_broker;

REVOKE ALL ON SCHEMA public FROM crosslink;
GRANT USAGE, CREATE ON SCHEMA crosslink_broker TO crosslink;

ALTER ROLE crosslink IN DATABASE okapi_modules
SET search_path = crosslink_broker, pg_temp;

CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;
