# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Calistenic — a local-only calisthenics workout tracker for Android (Kotlin + Jetpack Compose + Material 3 + Room + DataStore). No network code; all data lives on-device. UI strings and most code comments are in Turkish.

- `applicationId`: `com.samiozsoy.calistenic`, source package: `com.example.calistenic`
- `compileSdk`/`targetSdk` 35, `minSdk` 26, Java/Kotlin JVM target 17
- Version catalog is not used; versions are inlined in `app/build.gradle.kts`.

## Build / run / test

```bash
./gradlew assembleDebug                       # debug APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug                         # install on connected device/emulator
./gradlew lint                                 # Android lint
./gradlew test                                 # unit tests (JVM)
./gradlew connectedAndroidTest                 # instrumented tests (needs device/emulator)
./gradlew :app:assembleRelease                 # signed release APK (needs keystore.properties, see below)
./install_to_phone.sh                          # adb-install the built debug APK to one connected phone
```

Run a single test class or method:
```bash
./gradlew :app:testDebugUnitTest --tests "com.example.calistenic.SomeClass"
./gradlew :app:testDebugUnitTest --tests "com.example.calistenic.SomeClass.someMethod"
```

Signing: release builds read `keystore.properties` from the repo root (not committed). If absent, release builds are unsigned. Keys: `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. `install_to_phone.sh` requires `adb` (`brew install --cask android-platform-tools`) and exactly one authorized device.

## Architecture

Layered, single-module Compose app. Flow of data: `Compose screen → ViewModel → Repository → Room / DataStore`. Repositories expose `Flow`; ViewModels republish via `stateIn(WhileSubscribed(5_000))`. UIs collect these flows and recompose on change — there is no manual refresh path.

### Dependency graph (manual DI — no Hilt/Koin)
`CalistenicApplication` lazily constructs `WorkoutDatabase`, `WorkoutRepository`, `SettingsRepository`. `MainActivity` reads them off the `Application` and builds `WorkoutViewModel` via its `Factory`; `TimerViewModel` is a plain `AndroidViewModel`. Both VMs are passed into `CalistenicApp`, the root composable hosting the `NavHost`.

### Persistence
- `WorkoutEntity` (`workouts` table, Room v2, `exportSchema = true`) stores `sets: List<Int>` via `Converters` (CSV string). Schema JSONs live under `app/schemas/`.
- `WorkoutDatabase` is a singleton with `fallbackToDestructiveMigration(true)` — **schema bumps wipe user data**. The `MIGRATIONS` array is intentionally empty; add real migrations before bumping the version if data preservation matters.
- `SettingsRepository` uses `DataStore` preferences (name `settings`) for `defaultReps`, `restBetweenSets`, `restBetweenExercises`. Defaults: 6 / 25 / 120.

### Timer (foreground service + process-global state)
The timer is the only non-trivial Android-system interaction and spans three files:
- `TimerStateHolder` (singleton object) — single `MutableStateFlow<TimerUiState>` shared process-wide. Source of truth for the countdown.
- `TimerService` — foreground service (type `specialUse`). Owns the countdown coroutine and the looping `MediaPlayer` + vibrator alarm. Reads/writes `TimerStateHolder`. Started/stopped via intent actions declared as `ACTION_*` constants on the companion.
- `TimerViewModel` — bridges Compose to the service. Exposes `TimerStateHolder.state` to the UI and dispatches intents (`ACTION_START/PAUSE/RESUME/RESET/STOP/STOP_ALARM/SET_DURATION`). `setDuration` mutates state directly without starting the service (lets the user type a duration while idle); `start` starts the foreground service.

Because the service is foreground and state lives in `TimerStateHolder` (not the ViewModel), the timer survives config changes and screen-off. Manifest declares `FOREGROUND_SERVICE_SPECIAL_USE` with the required `<property>` subtype description (Turkish).

## Navigation & screens
`CalistenicApp` defines three bottom-nav routes: `add`, `history`, `settings`. `AddWorkoutScreen` takes both VMs (form + embedded timer); `WorkoutHistoryScreen` and `SettingsScreen` take only `WorkoutViewModel`. The history list groups by day and collapses/expands on tap (see README for the UX contract).

## Localization
`app/src/main/res/values/strings.xml` is the only string source and is in Turkish. Tab labels and screen text are referenced via `R.string.*` (`tab_add`, `tab_history`, `tab_settings`, `app_name`). There are no `values-*` qualifiers.

## ProGuard
`app/proguard-rules.pro` keeps Room entities/DAOs, DataStore, coroutines fields, `TimerUiState`, and `com.example.calistenic.**` members. Release uses `isMinifyEnabled = true` + `isShrinkResources = true` — when adding serializable/reflective types, extend the keep rules rather than disabling minification.

## Conventions
- Turkish comments throughout the source (e.g. `// Şema değişikliklerinde otomatik sıfırlama için...`). Match the language of surrounding code.
- No DI framework, no version catalog, no extra modules — keep it that way unless introducing a deliberate architectural change.
- User-facing strings go in `strings.xml`, not inline literals.