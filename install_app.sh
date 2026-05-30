#!/bin/bash

# Find adb executable path
ADB_PATH=$(which adb 2>/dev/null)

if [ -z "$ADB_PATH" ]; then
    if [ -f "$HOME/Android/Sdk/platform-tools/adb" ]; then
        ADB_PATH="$HOME/Android/Sdk/platform-tools/adb"
    elif [ -n "$ANDROID_HOME" ] && [ -f "$ANDROID_HOME/platform-tools/adb" ]; then
        ADB_PATH="$ANDROID_HOME/platform-tools/adb"
    else
        ADB_PATH="adb"
    fi
fi

# Locate the android directory containing gradlew
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ANDROID_DIR="$PROJECT_DIR/android"

if [ ! -d "$ANDROID_DIR" ]; then
    echo "Error: android directory not found at $ANDROID_DIR"
    exit 1
fi

echo "Checking for connected Android devices..."
DEVICES_COUNT=$("$ADB_PATH" devices | grep -v "List of devices attached" | grep -v "^$" | wc -l)

if [ "$DEVICES_COUNT" -eq 0 ]; then
    # Try with sudo if standard adb doesn't see any device (for USB permission issue resolution)
    echo "No devices detected with user adb. Trying with sudo adb..."
    DEVICES_COUNT=$(sudo "$ADB_PATH" devices | grep -v "List of devices attached" | grep -v "^$" | wc -l)
    
    if [ "$DEVICES_COUNT" -eq 0 ]; then
        echo "Error: No devices detected. Please plug in your phone, enable USB debugging, and allow the connection."
        exit 1
    else
        echo "Device detected using sudo adb. Proceeding..."
        USE_SUDO_ADB=true
    fi
else
    echo "$DEVICES_COUNT device(s) detected. Proceeding..."
    USE_SUDO_ADB=false
fi

echo "Compiling and installing debug build onto your device..."
cd "$ANDROID_DIR" || exit 1

# If device was only visible under sudo, we might need sudo for gradlew installation task
if [ "$USE_SUDO_ADB" = true ]; then
    # Run gradle with sudo to ensure it has root access to communicate with the root-owned adb daemon
    sudo ./gradlew installDebug
else
    ./gradlew installDebug
fi

if [ $? -eq 0 ]; then
    echo "============================================="
    echo "SUCCESS: SpotLyric has been installed on your phone!"
    echo "============================================="
else
    echo "Error: Installation failed."
    exit 1
fi
