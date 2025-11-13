#!/usr/bin/env bash
# Usage: source ./mindsdb-cloud-env.sh
# After editing MINDSDB_API_KEY below with your actual key, run:
#   source mindsdb-cloud-env.sh
# Then restart Spring Boot:
#   pkill -f bootRun || true
#   ./gradlew bootRun
# Verify:
#   curl -u admin:admin123 -X POST http://localhost:8080/api/mindsdb/query \
#     -H "Content-Type: application/json" \
#     -d '{"query":"SHOW DATABASES"}'

# === EDIT THIS VALUE ===
export MINDSDB_API_KEY="AIzaSyD7MhJ95U_T6hndo6eGF7qyDUaCMndI-C0"

# Optional: Uncomment to also persist to your ~/.bashrc automatically
# if ! grep -q "MINDSDB_API_KEY" ~/.bashrc; then
#   echo "export MINDSDB_API_KEY=\"$MINDSDB_API_KEY\"" >> ~/.bashrc
#   echo "Appended MINDSDB_API_KEY to ~/.bashrc"
# fi

echo "MINDSDB_API_KEY set (length: ${#MINDSDB_API_KEY}). Restart the app to apply."
