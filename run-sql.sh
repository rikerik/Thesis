#!/bin/bash

# Wait for PostgreSQL to start
echo "Waiting for PostgreSQL to be ready..."
while ! pg_isready -h localhost -U postgres; do
    sleep 2
done

# Execute the SQL file
echo "PostgreSQL is ready. Running database.sql..."
psql -U postgres -d postgres -f /docker-entrypoint-initdb.d/database.sql

echo "Database initialized successfully."
