#!/usr/bin/env bash
set -euo pipefail

# Run the Droid React Native app on an Android emulator (AVD).
# - Starts the emulator if no running emulator is detected
# - Waits for boot completion
# - Builds and installs the app
# - Launches the app's main activity

AVD_NAME="${AVD_NAME:-test_device}"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
ADB="${ADB:-adb}"

function is_emulator_running() {
  "$ADB" devices | awk 'NR>1 && $2=="device" { print $1 }' | grep -q '^emulator-' || return 1
}

# Start emulator if not present
if ! is_emulator_running; then
  echo "No running emulator detected. Starting AVD '$AVD_NAME'..."
  if command -v emulator >/dev/null 2>&1; then
    emulator -avd "$AVD_NAME" -gpu host -no-snapshot-load &>/tmp/droid-emulator.log &
  else
    SDK_EMULATOR="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Android/Sdk}}/emulator/emulator"
    if [ -x "$SDK_EMULATOR" ]; then
      "$SDK_EMULATOR" -avd "$AVD_NAME" -gpu host -no-snapshot-load &>/tmp/droid-emulator.log &
    else
      echo "error: emulator binary not found. Ensure Android SDK emulator is installed and 'emulator' is on PATH or ANDROID_SDK_ROOT/ANDROID_HOME is set."
      exit 2
    fi
  fi

  echo "Waiting up to 120s for emulator to boot..."
  for i in {1..120}; do
    sleep 1
    if is_emulator_running; then
      BOOT_COMPLETED=$($ADB shell getprop sys.boot_completed 2>/dev/null || echo 0)
      if [ "$BOOT_COMPLETED" = "1" ]; then
        echo "Emulator boot complete"
        break
      fi
    fi
    if [ $i -eq 120 ]; then
      echo "Timed out waiting for emulator to boot. Check /tmp/droid-emulator.log for details." >&2
      exit 3
    fi
  done
else
  echo "Found running emulator. Reusing it."
fi

# Start Metro bundler in the background if not already running
if ! lsof -i :8082 >/dev/null 2>&1; then
  echo "Starting Metro bundler on port 8082..."
  npx react-native start --port 8082 &>/tmp/metro-bundler.log &
  METRO_PID=$!
  echo "Metro bundler started (PID: $METRO_PID)"
  # Give Metro a few seconds to initialize
  sleep 3
else
  echo "Metro bundler already running on port 8082"
fi

# Build and install the app
cd "$PROJECT_DIR"
npx react-native run-android

echo "Launched Droid app. Log output for emulator is at /tmp/droid-emulator.log"
echo "Metro bundler log is at /tmp/metro-bundler.log"
exit 0
