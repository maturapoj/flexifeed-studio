#!/bin/bash
set -e

SDK_PATH=$(xcrun --sdk iphonesimulator --show-sdk-path)
FRAMEWORK_DIR="$(cd "$(dirname "$0")/.." && pwd)/app/build/bin/iosSimulatorArm64/debugFramework"
OUTPUT_DIR="$(cd "$(dirname "$0")/.." && pwd)/iosApp/build/FlexiFeed.app"

mkdir -p "$OUTPUT_DIR/Frameworks"

cp -R "$FRAMEWORK_DIR/ComposeApp.framework" "$OUTPUT_DIR/Frameworks/"
cp "$(cd "$(dirname "$0")/.." && pwd)/iosApp/iosApp/Info.plist" "$OUTPUT_DIR/Info.plist"

xcrun swiftc \
  -target arm64-apple-ios17.0-simulator \
  -sdk "$SDK_PATH" \
  -F "$FRAMEWORK_DIR" \
  -framework ComposeApp \
  -Xlinker -rpath -Xlinker @executable_path/Frameworks \
  -o "$OUTPUT_DIR/FlexiFeed" \
  $(cd "$(dirname "$0")/.." && pwd)/iosApp/iosApp/iOSApp.swift \
  $(cd "$(dirname "$0")/.." && pwd)/iosApp/iosApp/ContentView.swift

codesign --force --deep --sign - "$OUTPUT_DIR"

echo "FlexiFeed.app successfully built at $OUTPUT_DIR"
