#!/usr/bin/env bash
# Android SDK environment setup for Linux
# Usage: source ./android-env.sh

set -euo pipefail

# Default SDK path (detected)
ANDROID_SDK_DIR="/home/ubuntu/Android/Sdk"

if [ ! -d "$ANDROID_SDK_DIR" ]; then
  echo "Android SDK not found at $ANDROID_SDK_DIR" >&2
  echo "Update ANDROID_SDK_DIR in this script to your SDK location and re-run." >&2
  return 1 2>/dev/null || exit 1
fi

# Exports
export ANDROID_SDK_ROOT="$ANDROID_SDK_DIR"
export ANDROID_HOME="$ANDROID_SDK_DIR"

# Prefer 'latest' cmdline-tools when present, fallback to first available
CMDLINE_TOOLS_BIN="$ANDROID_SDK_DIR/cmdline-tools/latest/bin"
if [ ! -d "$CMDLINE_TOOLS_BIN" ]; then
  # Try any versioned cmdline-tools
  first_tool_dir=$(find "$ANDROID_SDK_DIR/cmdline-tools" -maxdepth 1 -mindepth 1 -type d 2>/dev/null | head -n1)
  if [ -n "${first_tool_dir:-}" ] && [ -d "$first_tool_dir/bin" ]; then
    CMDLINE_TOOLS_BIN="$first_tool_dir/bin"
  else
    CMDLINE_TOOLS_BIN=""
  fi
fi

# Build PATH entries
PATH_ENTRIES=(
  "$ANDROID_SDK_DIR/emulator"
  "$ANDROID_SDK_DIR/platform-tools"
)

if [ -n "$CMDLINE_TOOLS_BIN" ]; then
  PATH_ENTRIES+=("$CMDLINE_TOOLS_BIN")
fi

# Prepend entries to PATH if not already present
for dir in "${PATH_ENTRIES[@]}"; do
  case ":$PATH:" in
    *":$dir:"*) ;; # already there
    *) export PATH="$dir:$PATH" ;;
  esac
done

# Quick sanity output
echo "ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT"
echo "Added to PATH: ${PATH_ENTRIES[*]}"

# Optional: accept licenses non-interactively (commented out)
# yes | sdkmanager --licenses >/dev/null 2>&1 || true
