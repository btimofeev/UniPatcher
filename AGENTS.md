# AGENTS.md

## Project overview

UniPatcher is a ROM patcher for Android. It applies patches to ROM images
(IPS, IPS32, UPS, BPS, APS GBA, APS N64, PPF, DPS, EBP, XDelta3), creates
XDelta3 patches, fixes Sega Mega Drive checksums, and removes SMC headers
from SNES ROMs.

Open-source, GPL-3.0.

## Tech stack

- Kotlin 2.x + Jetpack Compose (Material 3), single-activity architecture, Navigation Compose
- Hilt for DI, ViewModels + Coroutines
- ViewBinding enabled but UI is Compose
- Native code: C (xdelta3 + liblzma) built via CMake/ndk-build
- minSdk 24, targetSdk 36, version 0.18
- Test: JUnit 4 + MockK, all in Kotlin, in `app/src/test`

## Build commands

Native deps are NOT in the repo (gitignored). Run first:

```bash
./gradlew downloadDependencies
```

Build variants use flavors `free` and `google`:

```bash
./gradlew assembleFreeDebug
./gradlew installFreeDebug
```

Full build (release flavors also apply towards Google Play / F-Droid):
```bash
./gradlew assembleFreeRelease  # requires signing.properties (not present → unsigned)
```

## Tests

```bash
./gradlew testFreeDebugUnitTest
```

Tests are JUnit 4 (all Kotlin) in `app/src/test/java/org/emunix/unipatcher/`.

## Patch format docs

Notes on some patch formats are in the wiki:
https://github.com/btimofeev/UniPatcher/wiki

## Project layout

- `app/src/main/java/org/emunix/unipatcher/`
  - `patcher/` — patch engines (BPS.kt, IPS.kt, XDelta.kt, PPF.java, ...) plus `PatcherFactory.kt`. Format implementations may be Kotlin or Java; keep matching existing style per file.
  - `tools/` — ROM operations (CreateXDelta3, SnesSmcHeader, SmdFixChecksum)
  - `ui/` — Compose screens (ApplyPatch, CreatePatch, Settings, Help, SnesSmcHeader, SmdFixChecksum, MainScreen)
  - `viewmodels/` — one ViewModel per screen
  - `utils/`, `helpers/`, `di/` — utilities, helpers (ResourceProvider, ThemeHelper, SocialHelper), Hilt modules
- `app/src/main/cpp/` — xdelta3 and xz (xz) native sources; `xdelta`/`xz` subdirs are downloaded
- `app/src/free/` and `app/src/google/` — flavor-specific code (e.g. DonateScreen)

## Conventions

- `Constants.kt` holds global constants; don't scatter string constants.
- Strings go to `res/values/strings.xml` (translations elsewhere; `MissingTranslation` lint is disabled).
- Source files carry a GPL-3.0 license header (see `Constants.kt`).
- Code is Kotlin-first; Java reused only for existing patcher files (PPF/DPS/APS_GBA etc.).
- Tests are Kotlin and use MockK for mocking.
- Existing code often uses functional helpers from `utils/Extensions.kt` and `utils/FileUtils.kt` — prefer those over rewriting.
- `ui/main/MainRoutes.kt` defines navigation routes.
- Don't reformat/rewrite files unrelated to the task; match local style.