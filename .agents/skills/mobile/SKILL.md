---
name: mobile
description: Comprehensive development, build, test, and debugging workflows for the FlexiFeed Kotlin Multiplatform (Compose Multiplatform) client across Android and iOS.
---

# FlexiFeed Mobile (KMP & Compose Multiplatform) — Agent Runbook

This skill provides complete development, build, test, and debugging workflows for **FlexiFeed**, a cross-platform Server-Driven UI (SDUI) mobile application targeting **Android** and **iOS** using **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (CMP)**.

---

## 1. Architecture & Code Sharing (>95% Shared)

FlexiFeed shares over 95% of UI and application logic across platforms in `FlexiFeed/app/src/commonMain/`:

```text
FlexiFeed/
├── app/
│   ├── build.gradle.kts           # KMP build definition (Android + iOS targets, dependencies)
│   └── src/
│       ├── commonMain/kotlin/     # Shared Compose UI, MVI ViewModels, Domain, Ktor API & SSE
│       │   ├── App.kt             # Root Multiplatform Composable with Koin Context
│       │   ├── data/              # SDUIApi (Ktor), SDUIStreamService (SSE), Repositories
│       │   ├── di/                # AppModules.kt (Shared Koin Dependency Injection)
│       │   ├── domain/            # Models, SDUIConstants, Actions, Analytics
│       │   ├── ui/                # SDUIRenderer, ComponentRegistry, Theme, Screens, Sheets
│       │   └── util/              # NetworkLogCollector, PlatformTime
│       ├── androidMain/kotlin/    # Android Host, Application, Chucker, Firebase
│       │   ├── MainActivity.kt    # Android Entry ComponentActivity
│       │   ├── FlexiFeedApp.kt    # Android Application class
│       │   └── di/                # PlatformModule.android.kt (Chucker, OkHttp engine)
│       ├── iosMain/kotlin/        # iOS Host, Framework export
│       │   ├── MainViewController.kt # UIViewController wrapper for Compose App()
│       │   └── di/                # PlatformModule.ios.kt (Darwin engine, Env resolution)
│       └── commonTest/kotlin/     # Cross-Platform Unit Tests (100% Pass Rate)
└── iosApp/                        # Pure Swift / Native Xcode Project (Zero CocoaPods)
    ├── iosApp.xcodeproj/          # Project & Shared Schemes (Dev, Local, Prod)
    ├── iosApp/ContentView.swift   # SwiftUI view embedding MainViewController
    ├── iosApp/Info.plist          # Configured with SDUI_BASE_URL & CFBundleDisplayName
    └── build_ios.sh               # CLI build script for iOS simulator
```

---

## 2. Multi-Environment Configurations

Both Android and iOS support three target environments:

| Environment | Purpose | Server Endpoint URL | Android Variant | iOS Scheme |
| :--- | :--- | :--- | :--- | :--- |
| **Local** | Local development with local Node server | `http://10.0.2.2:8080/` (Android)<br>`http://localhost:8080/` (iOS) | `localDebug` | `iosApp (Local)` |
| **Dev** (Default) | Remote develop server on Render cloud | `https://flexifeed-studio.onrender.com/` | `devDebug` | `iosApp (Dev)` |
| **Prod** | Production cloud server | `https://flexifeed-studio.onrender.com/` | `prodRelease` | `iosApp (Prod)` |

### How Environments Are Resolved
- **Android**: Generated via Gradle `buildConfigField("String", "SDUI_BASE_URL", ...)` based on build flavor (`dev`, `local`, `prod`).
- **iOS**: Resolved dynamically at runtime in `PlatformModule.ios.kt`:
  1. `ProcessInfo.processInfo.environment["SDUI_BASE_URL"]` (configured in Xcode Schemes)
  2. `NSBundle.mainBundle.objectForInfoDictionaryKey("SDUI_BASE_URL")` (configured in `Info.plist`)
  3. Default fallback: `https://flexifeed-studio.onrender.com/`

---

## 3. Android Development Runbook

### 3.1 Unit Testing (Mandatory 100% Pass Rate)
Always run tests before completing tasks or committing changes:
```bash
# From workspace root
make test

# Or directly in FlexiFeed directory
cd FlexiFeed && ./gradlew testDevDebugUnitTest
```
Suites in `FlexiFeed/app/src/commonTest/`:
- `HomeMviTest`: MVI state transitions, UI event contracts.
- `CartAndDITest`: Cart state mutation and Koin DI definitions.
- `SDUIRepositoryTest`: Live Ktor fetch and offline mock fallback.
- `ComponentRegistryTest`: SDUI component mapping and graceful fallbacks.
- `SDUIActionContractsTest`: SDUI actions (`NAVIGATE`, `ADD_TO_CART`, `ANALYTICS`).

### 3.2 Building and Installing Android APK
```bash
# Dev flavor (points to Render cloud backend)
make build          # ./gradlew assembleDevDebug
make install        # ./gradlew installDevDebug
make launch         # adb shell am start -n com.flexifeed.app.dev/com.flexifeed.app.MainActivity

# Local flavor (points to local Node server http://10.0.2.2:8080)
make build-local    # ./gradlew assembleLocalDebug
make install-local  # ./gradlew installLocalDebug
make launch-local   # adb shell am start -n com.flexifeed.app.local/com.flexifeed.app.MainActivity
```

### 3.3 Android Device Interaction & Inspection (ADB)
```bash
# Send tap or back key
adb shell input tap <X> <Y>
adb shell input keyevent 4 # KEYCODE_BACK

# Capture screenshot
adb exec-out screencap -p > /path/to/screenshot.png

# View live logcat filtered by FlexiFeed
adb logcat -v time -s FlexiFeed SDUIRepository NetworkLog
```

### 3.4 In-App & Network Debugging on Android
- **Chucker Interceptor**: Intercepts all OkHttp traffic in debug builds. Push notifications appear on device with request/response body, headers, and timings.
- **In-App Network Inspector Sheet**: Tap the `[i] <count>` badge in the top app bar to view the interactive network sheet across both platforms.

---

## 4. iOS Development Runbook (Zero CocoaPods)

> [!IMPORTANT]
> **Zero CocoaPods Rule**: FlexiFeed does **NOT** use CocoaPods, Podfiles, or third-party iOS package managers. The Xcode project is a pure Swift project linking to Kotlin Multiplatform framework via standard Xcode build phases.

### 4.1 Opening the Project in Xcode
```bash
# Open Xcode project from root
make ios-open
# Or directly
open FlexiFeed/iosApp/iosApp.xcodeproj
```

### 4.2 Building iOS via Command Line
```bash
# Build for iOS Simulator using the Dev scheme
xcodebuild \
  -project FlexiFeed/iosApp/iosApp.xcodeproj \
  -scheme "iosApp (Dev)" \
  -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  build

# Or use the convenience helper script
cd FlexiFeed/iosApp && ./build_ios.sh
```

### 4.3 Managing iOS Simulator (simctl)
```bash
# List available simulator runtimes and devices
xcrun simctl list devices available | grep iPhone

# Boot a specific simulator device
xcrun simctl boot <DEVICE_UUID>

# Open Simulator GUI app
open -a Simulator

# Install compiled .app onto booted simulator
xcrun simctl install booted /Users/<user>/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Products/Debug-iphonesimulator/iosApp.app

# Launch the app on booted simulator
xcrun simctl launch booted com.flexifeed.app

# Capture screenshot from simulator
xcrun simctl io booted screenshot /path/to/screenshot.png
```

### 4.4 Critical Xcode Configuration Settings
In `FlexiFeed/iosApp/iosApp.xcodeproj/project.pbxproj`:
- `ENABLE_USER_SCRIPT_SANDBOXING = NO`: **Must be NO** for both Debug and Release configurations. This allows the Gradle script phase (`./gradlew :app:embedAndSignAppleFrameworkForXcode`) to generate and copy the compose resources and `FlexiFeedApp.framework` without Xcode sandbox permission violations.
- Framework Search Paths: `$(SRCROOT)/../app/build/bin/iosSimulatorArm64/debugFramework`

---

## 5. In-App Debug Network Inspector (Cross-Platform)

Both Android and iOS include a built-in debug network sheet:
1. **Trigger Badge**: Located on the right side of the `HomeTopAppBar` displaying `[i] <count>` (e.g. `[i] 2`).
2. **Inspector Capabilities**:
   - Lists recent HTTP REST calls and SSE stream events.
   - Shows HTTP Method, Status Code (`200 OK`, `404`, etc.), URL, and Latency (ms).
   - Expandable request/response payloads with syntax-aware display.
   - "Clear Logs" action button to reset recorded requests.
3. **Connectivity Banner**:
   - Shows device network state: `Device: WIFI online` or `CELLULAR online`.
   - Displays active server target: `[DEV] https://flexifeed-studio.onrender.com/` or `[LOCAL] http://...`.

---

## 6. Common Issues & Troubleshooting

| Issue | Root Cause | Solution |
| :--- | :--- | :--- |
| `XCBuildData/build.db: database is locked` | An earlier background xcodebuild held the database lock | Remove directory: `rm -rf ~/Library/Developer/Xcode/DerivedData/iosApp-*/Build/Intermediates.noindex/XCBuildData` |
| Sandbox error running `./gradlew embedAndSignAppleFramework` | Xcode 15+ user script sandboxing enabled | Ensure `ENABLE_USER_SCRIPT_SANDBOXING = NO` in `project.pbxproj` |
| Gradle build cache issues | Stale intermediate KMP artifact caches | Run `cd FlexiFeed && ./gradlew clean` (or `make clean`) |
| Local server connection refused | Android emulator requires loopback alias | Use `http://10.0.2.2:8080/` on Android emulator and `http://localhost:8080/` on iOS simulator |
