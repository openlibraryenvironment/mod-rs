select 1;

CREATE USER folio_admin WITH PASSWORD 'folio_admin';
DROP DATABASE if exists okapi_modules;
CREATE DATABASE okapi_modules;
GRANT ALL PRIVILEGES ON DATABASE okapi_modules to folio_admin;

ALTER USER folio_admin CREATEDB;
ALTER USER folio_admin CREATEROLE;
ALTER USER folio_admin WITH SUPERUSER;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
