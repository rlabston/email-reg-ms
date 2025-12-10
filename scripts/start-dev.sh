#!/usr/bin/env bash
set -euo pipefail

# scripts/start-dev.sh
# Build and start the backend and gateway for local development.
# - Builds backend (:bootJar) and gateway (:gateway:bootJar)
# - Starts backend jar (background), waits for port 8081
# - Starts gateway jar (background), waits for port 8080
# - Writes pids to scripts/backend.pid and scripts/gateway.pid and logs to logs/

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

LOG_DIR="${LOG_DIR:-$ROOT/logs}"
PID_DIR="${PID_DIR:-$ROOT/scripts}"
mkdir -p "$LOG_DIR" "$PID_DIR"

echo "Building backend and gateway jars (may take a while)..."
./gradlew :bootJar :gateway:bootJar -x test --no-daemon

## Prefer the Spring Boot fat jar (without "-plain") if present, otherwise fall back to any jar.
BACKEND_JAR="$(ls -1 "$ROOT"/build/libs/*.jar 2>/dev/null | grep -v -- "-plain.jar" | head -n1 || true)"
if [ -z "$BACKEND_JAR" ]; then
  BACKEND_JAR="$(ls -1 "$ROOT"/build/libs/*.jar 2>/dev/null | head -n1 || true)"
fi

GATEWAY_JAR="$(ls -1 "$ROOT"/gateway/build/libs/*.jar 2>/dev/null | grep -v -- "-plain.jar" | head -n1 || true)"
if [ -z "$GATEWAY_JAR" ]; then
  GATEWAY_JAR="$(ls -1 "$ROOT"/gateway/build/libs/*.jar 2>/dev/null | head -n1 || true)"
fi

if [ -z "$BACKEND_JAR" ] || [ -z "$GATEWAY_JAR" ]; then
  echo "Error: could not find built jars. Make sure the build succeeded." >&2
  ls -lah "$ROOT"/build/libs || true
  ls -lah "$ROOT"/gateway/build/libs || true
  exit 1
fi

# Parse optional flags (default to FORCE=true for reliability)
FORCE=true
while [[ $# -gt 0 ]]; do
  case "$1" in
    --force)
      FORCE=true
      shift
      ;;
    --no-force)
      FORCE=false
      shift
      ;;
    *)
      echo "Unknown argument: $1" >&2
      shift
      ;;
  esac
done

check_port_listener() {
  local port=$1
  ss -ltn 2>/dev/null | grep -q ":${port}\b"
}

start_and_wait() {
  local jar=$1; local port=$2; local name=$3
  echo "Starting $name from: $jar"
  nohup java -jar "$jar" > "$LOG_DIR/${name}.log" 2>&1 &
  local pid=$!
  echo $pid > "$PID_DIR/${name}.pid"

  echo -n "Waiting for $name to listen on port $port"
  for i in $(seq 1 60); do
    if check_port_listener "$port"; then
      echo " -> up (pid=$pid)"
      return 0
    fi
    echo -n "."
    sleep 1
  done
  echo
  echo "Timed out waiting for $name to listen on port $port" >&2
  return 1
}

echo "Backend jar: $BACKEND_JAR"
echo "Gateway jar: $GATEWAY_JAR"

if check_port_listener 8081; then
  echo "Warning: port 8081 already has a listener. The script will still attempt to start the backend but will not kill the existing process."
fi
if check_port_listener 8080; then
  echo "Warning: port 8080 already has a listener. The script will still attempt to start the gateway but will not kill the existing process."
fi

if [ "$FORCE" = true ]; then
  echo "--force specified: will kill any processes listening on ports 8081 and 8080 before starting."
  for p in 8081 8080; do
    # Try to find PIDs via lsof (preferred) then fallback to ss+grep parsing if lsof not available
    if command -v lsof >/dev/null 2>&1; then
      pids=$(lsof -ti:"$p" || true)
    else
      # ss output like: LISTEN 0 100 [::ffff:127.0.0.1]:8081 *:* users:("java",pid=123,fd=87)
      pids=$(ss -ltnp 2>/dev/null | grep ":${p}\b" | sed -n 's/.*pid=\([0-9]*\),.*/\1/p' | tr '\n' ' ' || true)
    fi
    if [ -n "$pids" ]; then
      echo "Killing processes listening on port $p: $pids"
      kill -9 $pids || true
      sleep 1
    else
      echo "No processes found listening on port $p"
    fi
  done
fi

start_and_wait "$BACKEND_JAR" 8081 backend || { echo "Backend failed to start"; exit 1; }
start_and_wait "$GATEWAY_JAR" 8080 gateway || { echo "Gateway failed to start"; exit 1; }

echo "All services started successfully."
echo "Backend PID: $(cat "$PID_DIR/backend.pid")  -> logs: $LOG_DIR/backend.log"
echo "Gateway PID:  $(cat "$PID_DIR/gateway.pid")   -> logs: $LOG_DIR/gateway.log"
echo "Use 'tail -f $LOG_DIR/backend.log' and 'tail -f $LOG_DIR/gateway.log' to follow logs."

exit 0
