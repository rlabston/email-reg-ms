#!/usr/bin/env bash
set -euo pipefail

# Run the AI Catalog app on an Android emulator (AVD).
# - Starts the emulator if no running emulator is detected
# - Waits for boot completion
# - Builds the debug APK if missing and installs it
# - Launches the app's launcher activity

AVD_NAME="${AVD_NAME:-test_device}"
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
ADB="${ADB:-adb}"

echo "ai-catalog: running on AVD='$AVD_NAME' (project dir: $PROJECT_DIR)"

function is_emulator_running() {
  "$ADB" devices | awk 'NR>1 && $2=="device" { print $1 }' | grep -q '^emulator-' || return 1
}

# Start emulator if not present
if ! is_emulator_running; then
  echo "No running emulator detected. Starting AVD '$AVD_NAME'..."
  if command -v emulator >/dev/null 2>&1; then
    emulator -avd "$AVD_NAME" -gpu host -no-snapshot-load &>/tmp/ai-catalog-emulator.log &
  else
    # try common SDK paths
    SDK_EMULATOR="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Android/Sdk}}/emulator/emulator"
    if [ -x "$SDK_EMULATOR" ]; then
      "$SDK_EMULATOR" -avd "$AVD_NAME" -gpu host -no-snapshot-load &>/tmp/ai-catalog-emulator.log &
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
      echo "Timed out waiting for emulator to boot. Check /tmp/ai-catalog-emulator.log for details." >&2
      exit 3
    fi
  done
else
  echo "Found running emulator. Reusing it."
fi

# Ensure APK exists (build if necessary)
if [ ! -f "$APK_PATH" ]; then
  echo "APK not found at $APK_PATH — building app (assembleDebug)"
  (cd "$PROJECT_DIR" && ./gradlew :app:assembleDebug --no-daemon --console=plain)
fi

echo "Installing APK: $APK_PATH"
"$ADB" install -r "$APK_PATH"

echo "Launching app package 'com.android.ai.catalog'"
"$ADB" shell monkey -p com.android.ai.catalog -c android.intent.category.LAUNCHER 1

echo "Launched. To capture a screenshot run: adb exec-out screencap -p > ai-catalog/app-screenshot.png"
echo "Log output for emulator is at /tmp/ai-catalog-emulator.log"

exit 0
