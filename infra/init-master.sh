#!/bin/bash
set -e

require_env() {
  if [ -z "${!1}" ]; then
    echo "$1 is required"
    exit 1
  fi
}

for var in APP_DB_NAME APP_DB_USERNAME APP_DB_PASSWORD TEST_DB_NAME REPL_PASSWORD POSTGRES_USER POSTGRES_DB; do
  require_env "$var"
done

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
