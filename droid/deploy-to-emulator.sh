#!/usr/bin/env bash
# Deploy Droid React Native app to Android emulator
# Builds release APK (no Metro server needed) and installs to running emulator

set -euo pipefail

# Use Java 21 for Android builds (React Native CMake compatibility)
# Backend/Gateway use Java 25, but Android builds require Java 21
export JAVA_HOME=/usr/lib/jvm/java-1.21.0-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

echo "Using Java version for Android build:"
java -version
echo ""

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
ANDROID_DIR="$PROJECT_DIR/android"
APK_PATH="$ANDROID_DIR/app/build/outputs/apk/release/app-release.apk"
PACKAGE_NAME="com.droid"
MAIN_ACTIVITY=".MainActivity"

echo "=== Droid App Deployment Script ==="
echo "Project: $PROJECT_DIR"
echo ""

# Check if emulator is running
if ! adb devices | grep -q "emulator.*device"; then
  echo "ERROR: No Android emulator running"
  echo "Start an emulator first, then run this script"
  exit 1
fi

echo "✓ Emulator detected"
echo ""

# Build release APK
echo "Building release APK..."
cd "$ANDROID_DIR"
./gradlew assembleRelease

if [ ! -f "$APK_PATH" ]; then
  echo "ERROR: APK not found at $APK_PATH"
  exit 2
fi

echo "✓ APK built successfully: $APK_PATH"
echo ""

# Install APK
echo "Installing APK to emulator..."
adb install -r "$APK_PATH"

if [ $? -ne 0 ]; then
  echo "ERROR: Failed to install APK"
  exit 3
fi

echo "✓ APK installed"
echo ""

# Launch app
echo "Launching $PACKAGE_NAME$MAIN_ACTIVITY..."
adb shell am start -n "$PACKAGE_NAME/$MAIN_ACTIVITY"

if [ $? -ne 0 ]; then
  echo "WARNING: App may have launched but command returned non-zero"
fi

echo ""
echo "=== Deployment Complete ==="
echo "View logs with: adb logcat | grep ReactNative"
echo "Stop app with: adb shell am force-stop $PACKAGE_NAME"
