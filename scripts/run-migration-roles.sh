#!/usr/bin/env bash
# Run the role migration SQL against a MySQL instance.
# Usage:
#   ./scripts/run-migration-roles.sh [DB_USER] [DB_PASS] [DB_NAME] [DB_HOST] [DB_PORT]
# Defaults are: emailapp emailapp123 email_reg_db 127.0.0.1 3306

set -euo pipefail

DB_USER=${1:-emailapp}
DB_PASS=${2:-emailapp123}
DB_NAME=${3:-email_reg_db}
DB_HOST=${4:-127.0.0.1}
DB_PORT=${5:-3306}

SQL_FILE="$(dirname "$0")/../db/migrations/20251117_migrate_registered_email_roles_to_user_role_link.sql"

if [ ! -f "$SQL_FILE" ]; then
  echo "Migration SQL not found: $SQL_FILE" >&2
  exit 1
fi

echo "Running migration: $SQL_FILE against ${DB_USER}@${DB_HOST}:${DB_PORT}/${DB_NAME}"
mysql -u "$DB_USER" -p"$DB_PASS" -h "$DB_HOST" -P "$DB_PORT" "$DB_NAME" < "$SQL_FILE"

echo "Migration completed."
