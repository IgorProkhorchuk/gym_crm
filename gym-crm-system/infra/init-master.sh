#!/bin/bash
set -e

APP_DB_NAME="${APP_DB_NAME:-gym_crm}"
APP_DB_USERNAME="${APP_DB_USERNAME:-gym_user}"
APP_DB_PASSWORD="${APP_DB_PASSWORD:-password}"
TEST_DB_NAME="${TEST_DB_NAME:-gym_crm_test}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    SELECT 'CREATE ROLE repl_user WITH REPLICATION LOGIN PASSWORD ''$REPL_PASSWORD'''
    WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'repl_user')\gexec

    SELECT 'CREATE ROLE $APP_DB_USERNAME WITH LOGIN PASSWORD ''$APP_DB_PASSWORD'''
    WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$APP_DB_USERNAME')\gexec

    ALTER ROLE $APP_DB_USERNAME WITH PASSWORD '$APP_DB_PASSWORD';

    SELECT 'CREATE DATABASE $APP_DB_NAME OWNER $APP_DB_USERNAME'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$APP_DB_NAME')\gexec

    SELECT 'CREATE DATABASE $TEST_DB_NAME OWNER $APP_DB_USERNAME'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$TEST_DB_NAME')\gexec
EOSQL

PGPASSWORD="$APP_DB_PASSWORD" psql -v ON_ERROR_STOP=1 --username "$APP_DB_USERNAME" --dbname "$APP_DB_NAME" -f /schema.sql
PGPASSWORD="$APP_DB_PASSWORD" psql -v ON_ERROR_STOP=1 --username "$APP_DB_USERNAME" --dbname "$APP_DB_NAME" -f /data.sql

PGPASSWORD="$APP_DB_PASSWORD" psql -v ON_ERROR_STOP=1 --username "$APP_DB_USERNAME" --dbname "$TEST_DB_NAME" -f /schema.sql
PGPASSWORD="$APP_DB_PASSWORD" psql -v ON_ERROR_STOP=1 --username "$APP_DB_USERNAME" --dbname "$TEST_DB_NAME" -f /data.sql

echo "host replication repl_user 0.0.0.0/0 scram-sha-256" >> "$PGDATA/pg_hba.conf"

echo "Master initialization completed!"
