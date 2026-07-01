#!/bin/bash
set -e

if [ -z "$POSTGRES_USER" ]; then
  echo "POSTGRES_USER is required"
  exit 1
fi

if [ -z "$REPL_PASSWORD" ]; then
  echo "REPL_PASSWORD is required"
  exit 1
fi

until pg_isready -h gym-master -p 5432 -U "$POSTGRES_USER"; do
  echo "Waiting for master to be ready..."
  sleep 2
done

if [ ! -s "$PGDATA/PG_VERSION" ]; then
  echo "Replica data is empty. Starting pg_basebackup..."
  rm -rf "$PGDATA"/*

  PGPASSWORD="$REPL_PASSWORD" pg_basebackup -h gym-master -p 5432 -U repl_user -D "$PGDATA" -Fp -Xs -R

  chmod 700 "$PGDATA"
  echo "Backup completed successfully!"
else
  echo "Replica data already exists. Skipping pg_basebackup."
fi

exec docker-entrypoint.sh postgres
