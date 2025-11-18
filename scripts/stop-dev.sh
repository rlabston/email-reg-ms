#!/usr/bin/env bash
set -euo pipefail

# scripts/stop-dev.sh
# Stop services started by scripts/start-dev.sh
# - Reads pid files in scripts/*.pid
# - Tries graceful shutdown (SIGTERM), waits, then SIGKILL if necessary
# - Records actions to logs/stop.log and any exceptions/errors to logs/stop-exceptions.log

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

LOG_DIR="${LOG_DIR:-$ROOT/logs}"
PID_DIR="${PID_DIR:-$ROOT/scripts}"
mkdir -p "$LOG_DIR" "$PID_DIR"

STOP_LOG="$LOG_DIR/stop.log"
ERR_LOG="$LOG_DIR/stop-exceptions.log"

timestamp() { date -u +"%Y-%m-%dT%H:%M:%SZ"; }

echo "Stopping services (pid files in $PID_DIR)" | tee -a "$STOP_LOG"

# parse optional flags
FORCE=false
while [[ $# -gt 0 ]]; do
  case "$1" in
    --force)
      FORCE=true
      shift
      ;;
    *)
      echo "Unknown argument: $1" | tee -a "$ERR_LOG"
      shift
      ;;
  esac
done

shopt -s nullglob
pidfiles=("$PID_DIR"/*.pid)

graceful_kill() {
  local pid=$1
  local name=$2
  echo "[$(timestamp)] $name: attempting graceful shutdown of pid $pid" | tee -a "$STOP_LOG"
  # exponential backoff: 1,2,4,8,16 (total ~31s)
  local delays=(1 2 4 8 16)
  if kill -15 "$pid" >/dev/null 2>&1; then
    for d in "${delays[@]}"; do
      if ! kill -0 "$pid" >/dev/null 2>&1; then
        echo "[$(timestamp)] $name: pid $pid exited after SIGTERM" | tee -a "$STOP_LOG"
        return 0
      fi
      sleep "$d"
    done
  else
    echo "[$(timestamp)] $name: failed to send SIGTERM to $pid" | tee -a "$ERR_LOG"
  fi

  # If still running, escalate
  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "[$(timestamp)] $name: pid $pid still running; sending SIGKILL" | tee -a "$STOP_LOG"
    if kill -9 "$pid" >/dev/null 2>&1; then
      echo "[$(timestamp)] $name: pid $pid killed (SIGKILL)" | tee -a "$STOP_LOG"
      return 0
    else
      echo "[$(timestamp)] $name: failed to kill pid $pid" | tee -a "$ERR_LOG"
      return 1
    fi
  fi

  return 0
}

stop_pidfile() {
  local pf=$1
  name="$(basename "$pf" .pid)"
  pid="$(cat "$pf" 2>/dev/null || echo "")"
  if [ -z "$pid" ]; then
    echo "[$(timestamp)] $name: pid file empty or unreadable ($pf)" | tee -a "$ERR_LOG"
    return
  fi

  if ! kill -0 "$pid" >/dev/null 2>&1; then
    echo "[$(timestamp)] $name: process $pid not running; removing pid file" | tee -a "$STOP_LOG"
    rm -f "$pf"
    return
  fi

  if graceful_kill "$pid" "$name"; then
    if ! kill -0 "$pid" >/dev/null 2>&1; then
      echo "[$(timestamp)] $name: process $pid stopped; removing pid file $pf" | tee -a "$STOP_LOG"
      rm -f "$pf"
    else
      echo "[$(timestamp)] $name: process $pid still exists after escalation" | tee -a "$ERR_LOG"
    fi
  else
    echo "[$(timestamp)] $name: failed to stop pid $pid" | tee -a "$ERR_LOG"
  fi
}

if [ ${#pidfiles[@]} -eq 0 ]; then
  if [ "$FORCE" = true ]; then
    echo "No pid files found, but --force specified: attempting to stop processes listening on ports 8081 and 8080" | tee -a "$STOP_LOG"
    # find pids by ports and attempt to stop them
    for p in 8081 8080; do
      if command -v lsof >/dev/null 2>&1; then
        pids=$(lsof -ti:"$p" || true)
      else
        pids=$(ss -ltnp 2>/dev/null | grep ":${p}\b" | sed -n 's/.*pid=\([0-9]*\),.*/\1/p' | tr '\n' ' ' || true)
      fi
      if [ -n "$pids" ]; then
        for pid in $pids; do
          name="port_${p}_pid_${pid}"
          if kill -0 "$pid" >/dev/null 2>&1; then
            if graceful_kill "$pid" "$name"; then
              echo "[$(timestamp)] $name: stopped" | tee -a "$STOP_LOG"
            else
              echo "[$(timestamp)] $name: failed to stop" | tee -a "$ERR_LOG"
            fi
          fi
        done
      else
        echo "No processes found listening on port $p" | tee -a "$STOP_LOG"
      fi
    done
  else
    echo "No pid files found in $PID_DIR" | tee -a "$STOP_LOG"
    exit 0
  fi
else
  for pf in "${pidfiles[@]}"; do
    stop_pidfile "$pf"
  done
fi

echo "Stop completed at $(timestamp)" | tee -a "$STOP_LOG"

exit 0
