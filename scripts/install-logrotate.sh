#!/usr/bin/env bash
set -euo pipefail

# scripts/install-logrotate.sh
# Renders the logrotate template and installs it to /etc/logrotate.d/email-reg-ms
# Usage: sudo bash scripts/install-logrotate.sh

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TEMPLATE="$ROOT_DIR/scripts/logrotate/email-reg-ms.conf.template"
OUT_FILE="/etc/logrotate.d/email-reg-ms"

if [ ! -f "$TEMPLATE" ]; then
  echo "Template not found: $TEMPLATE" >&2
  exit 1
fi

if [ "$EUID" -ne 0 ]; then
  echo "This script must be run as root to write to /etc/logrotate.d (use sudo)" >&2
  exit 1
fi

sed "s#@ROOT@#${ROOT_DIR}#g" "$TEMPLATE" > "$OUT_FILE"
chmod 644 "$OUT_FILE"
echo "Installed logrotate config to $OUT_FILE"
echo "You can test it with: logrotate --debug $OUT_FILE"
