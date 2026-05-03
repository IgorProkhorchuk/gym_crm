#!/bin/bash
set -e

APP_DB_NAME="${APP_DB_NAME:-gym_crm}"
APP_DB_USERNAME="${APP_DB_USERNAME:-gym_user}"
APP_DB_PASSWORD="${APP_DB_PASSWORD:-password}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE ROLE repl_user WITH REPLICATION LOGIN PASSWORD '$REPL_PASSWORD';
    CREATE ROLE $APP_DB_USERNAME WITH LOGIN PASSWORD '$APP_DB_PASSWORD';
    CREATE DATABASE $APP_DB_NAME OWNER $APP_DB_USERNAME;
EOSQL

echo "host replication repl_user 0.0.0.0/0 scram-sha-256" >> "$PGDATA/pg_hba.conf"

echo "Master initialization completed!"
