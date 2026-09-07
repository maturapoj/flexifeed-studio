# FlexiFeed — Agent Guidelines & Repository Instructions

Welcome to **FlexiFeed**, an Android Server-Driven UI (SDUI) sample and production architecture codebase. This document outlines architectural standards, component conventions, testing protocols, and development workflows for AI agents working in this repository.

---

## Thought Process Before Implementation

Before writing any code, always present a brief solution plan and wait for approval:
1. **Restate the problem**
   - Summarize the user's request and intent in your own words to ensure complete alignment.
   - Identify the root cause (bug, architecture bottleneck, or missing feature) rather than just treating symptoms.
   - Clarify the current behavior vs. expected behavior and affected layers (UI, ViewModel, SDUI DSL, or Backend).

2. **Proposed approach**
   - Outline a clear, incremental step-by-step implementation plan driven by TDG/TDD (tests first).
   - Identify specific files to create, modify, or delete across Domain, Data, and Presentation layers.
   - Specify architectural changes (StateFlow contracts, MVI events, SDUI DSL schemas, or component registry entries).

3. **Tradeoffs / alternatives**
   - Compare the primary solution against at least one viable alternative (e.g., client-side handling vs. server DSL payload).
   - Evaluate trade-offs regarding performance (Compose recomposition), maintainability, and backward compatibility.
   - Highlight potential side effects or breaking changes to existing tests and mock fallbacks.

4. **Stop and ask**
   - Explicitly pause and wait for the user's approval before modifying any code or running non-read-only commands.
   - Surface any ambiguities, open design questions, or architectural decisions that require user preference.
   - Ask for confirmation with a clear summary question (e.g., *"Shall I proceed with this plan?"*).

---

## Test-Driven Generation & Development (TDG / TDD)

Drive all code implementation with **TDG (Test-Driven Generation / Development)**:
1. **Write Tests First**: Define unit tests, contract specs, and edge-case assertions before writing or modifying implementation code.
2. **Minimal & Robust Code**: Write the cleanest code necessary to pass all assertions while adhering to Clean Architecture (no UseCase boilerplate).
3. **Refactor & Verify**: Eliminate code smells, enforce type safety (`SDUIConstants`), and ensure 100% test pass rate (`./gradlew testDevDebugUnitTest`).
4. **Prevent Regressions**: When resolving bugs or fixing UI edge cases, write a reproducing unit test first to safeguard the contract.

---

## 1. Project Overview & Structure

FlexiFeed is a monorepo consisting of:
- **`FlexiFeed/`**: Android application written in Kotlin with Jetpack Compose.
  - Package root: `com.flexifeed.app`
  - Flavors: `dev` (default dev build with mock fallback and live SSE) & `prod`
- **`server/`**: Lightweight Node.js Server-Driven UI API and Studio Backoffice.
  - Port: `8080` (HTTP API + SSE `/api/v1/stream`)
  - Backoffice GUI: `http://localhost:8080` (`server/public/`)
  - Feed Presets & Screens: `server/data/` (`feed.json`, `presets/`, `screens/`)

---

## 2. Technology Stack & Key Libraries

- **Kotlin**: 2.0+
- **Android Target**: Min SDK 26 (Android 8.0+), Target SDK 34 (Android 14)
- **UI Framework**: Jetpack Compose (BOM) with Material 3
- **Dependency Injection**: Koin (`koin-android`, `koin-androidx-compose`)
- **Networking**: Retrofit 2 + Gson converter, OkHttp 3 with SSE / Chunked streaming
- **Image Loading**: Coil 3 (`coil-compose`, `coil-network-okhttp`)
- **Navigation**: Jetpack Navigation Compose (`FlexiFeedNavGraph`)
- **Asynchrony & State**: Kotlin Coroutines (`viewModelScope`) & StateFlow (`StateFlow<HomeUiState>`)

---

## 3. Core Architecture Principles

### 3.1 Clean Architecture Without UseCases
- **Pattern**: Repository Pattern directly exposed to ViewModels.
- **Rule**: Do **NOT** create boilerplate UseCase / Interactor classes (e.g., `FetchHomeFeedUseCase`). Domain logic and business rules reside cleanly in Domain Models, Repositories (`SDUIRepository`), and ViewModels.

### 3.2 MVI (Model-View-Intent) & State Hoisting
- Each screen (e.g. `HomeScreen`, `SDUIGenericScreen`) follows MVI:
  - **State**: Single immutable data class (e.g., `HomeUiState`).
  - **Events**: Sealed interface for user intents (e.g., `HomeEvent`).
  - **Content**: Stateless Composables (e.g., `HomeScreenContent`) that hoist all state up and emit lambdas down (`onEvent`, `onAction`).

### 3.3 Action Dispatching & Navigation Separation
- UI interaction actions (`SDUIAction`) must be routed through **`ActionDispatcher`**:
  - `NAVIGATE`: Parses target deep link / URL (`flexifeed://product/201`, `flexifeed://campaign/mega-sale`) and navigates directly using `NavController.navigate("sdui/$screenId")`.
  - `ADD_TO_CART`: Adds product to `CartViewModel` with enriched name, price, and thumbnail.
  - `ANALYTICS`: Dispatches tracking events to `AnalyticsTracker`.
- **CRITICAL**: Do **NOT** bind `NAVIGATE` actions to preview bottom sheets (`targetNavigationUrl`) during native screen navigation. Preview bottom sheets (`NavigationPreviewSheet`) are strictly for explicit link inspection or unsupported fallback links.

### 3.4 Feed Caching & Back Navigation
- `HomeViewModel` preserves `cachedHomeFeed` across route transitions.
- When popping back stack from a detail/campaign screen to `home`, `HomeViewModel` restores the feed instantly without showing loading spinners or network flickers.

---

## 4. SDUI Design System & Component Registry

### 4.1 Type Safety & Constants
- Never use raw string literals for component types, action types, or prop keys.
- Always use `SDUIConstants` from `com.flexifeed.app.domain.model.SDUIConstants`:
  - Component Types: `SDUIConstants.ComponentType.CAROUSEL`, `HORIZONTAL_LIST`, `GRID_2X2`, `PRODUCT_CARD_COMPACT`, `PRODUCT_CARD_FULL`, `TEXT`, `IMAGE`, `BUTTON`, `ROW`, `COLUMN`, `SPACER`.
  - Action Types: `SDUIConstants.ActionType.NAVIGATE`, `ADD_TO_CART`, `ANALYTICS`.
  - Prop Keys: `SDUIConstants.PropKey.*`.

### 4.2 Component Registry
- New UI components must be registered in [ComponentRegistry.kt](file:///Users/ar677232/Documents/Android/Compose-server-render/FlexiFeed/app/src/main/java/com/flexifeed/app/ui/sdui/ComponentRegistry.kt).
- Unknown components must log a warning and render a graceful fallback or empty container rather than crashing.

### 4.3 Atomic Components
- Use helper extensions on `SDUINode` (`node.getString()`, `node.getInt()`, `node.resolvedImageUrl`) instead of manual casting (`props["key"] as? String`).
- Support hex colors using `parseHexColorOrNull` with safe fallback to `MaterialTheme.colorScheme`.

---

## 5. Development & Testing Commands

### 5.1 Unit Tests (Mandatory 100% Pass Rate)
Run unit tests from the `FlexiFeed/` directory:
```bash
./gradlew testDevDebugUnitTest
```
Ensure all tests pass before completing any task. Key test suites:
- `HomeMviTest`: Verifies MVI event handling and state transitions.
- `CartAndDITest`: Tests cart mutation logic and Koin dependency injection.
- `SDUIRepositoryTest`: Tests live API execution and mock fallback.
- `SDUIActionContractsTest`: Tests action schema and payload contracts.

### 5.2 Build & Install APK
```bash
# Build dev debug APK
./gradlew assembleDevDebug

# Install on connected emulator / device
./gradlew installDevDebug
```

### 5.3 Device / Emulator Interaction (ADB)
```bash
# Launch MainActivity
adb shell am start -n com.flexifeed.app.dev/com.flexifeed.app.MainActivity

# Send tap / key events
adb shell input tap <X> <Y>
adb shell input keyevent 4 # KEYCODE_BACK

# Capture screenshot for visual inspection
adb exec-out screencap -p > <output_path>.png
```

### 5.4 Backend Server
```bash
# Start Node.js SDUI Server (port 8080)
node server/server.js
```

---

## 6. Code Style & Quality Guidelines

1. **No Double-Bang (`!!`)**: Avoid `!!` assertions in Kotlin code. Use idiomatic `?.let`, `?:`, `checkNotNull`, or `requireNotNull` with clear explanations.
2. **Safe Fallbacks**: Data repositories must gracefully fall back to `MockSDUIService` when the Node.js server is unreachable.
3. **Preserve Comments & Docs**: Do not remove existing explanatory comments, docstrings, or license headers unless explicitly asked.
4. **Clean Git Status**: Remove temporary debug files and scratch scripts before finalizing changes.
