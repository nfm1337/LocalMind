# AGENTS.md

Guidance for coding agents working in this repository.

## Project Overview

LocalMind is an Android prototype for on-device RAG over personal notes. The
current focus is proving that the generation model and embedding model can run
together on a mid-range Android device before the full notes and retrieval
layers are added.

Read these files first when changing product behavior or architecture:

- `README.md`
- `docs/plan.md`
- `docs/architecture.md`

Also read and follow `docs/agent-collaboration.md` for the project-specific
pair-programming and review workflow before changing product behavior,
architecture, scope, or implementation.

## Repository Layout

- `app/` - Android application module.
- `app/src/main/java/il/nfm/localmind/` - Kotlin app, Compose UI, and ML wrappers.
- `app/src/main/cpp/` - Native tokenizer bridge and CMake configuration.
- `build-logic/` - Local Gradle convention plugins.
- `config/detekt/` - Detekt configuration.
- `docs/` - Product and architecture notes.
- `models/` - Model manifest and local model file locations.
- `scripts/` - Model download and device push scripts.
- `gradle/libs.versions.toml` - Central dependency versions.

## Development Commands

Use the Gradle wrapper from the repository root:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew ktlintCheck
./gradlew ktlintFormat
./gradlew detekt
```

Model-related tasks:

```bash
./gradlew downloadModels
./gradlew pushModels
./gradlew downloadAndPushModels
```

`pushModels` requires `adb` and a connected Android device. The native tokenizer
build requires the Android SDK/NDK, CMake, Rust, and the
`aarch64-linux-android` Rust target.

## Coding Guidelines

- Prefer the existing Kotlin, Gradle Kotlin DSL, and Jetpack Compose patterns.
- Keep changes small and aligned with the current prototype scope.
- Put shared build behavior in `build-logic/` only when it is genuinely shared.
- Keep dependency versions centralized in `gradle/libs.versions.toml`.
- Do not hard-code model paths in app code; use the manifest/script conventions
  already documented in `README.md`.
- Preserve the current Android package/application id: `il.nfm.localmind`.
- Preserve the current `arm64-v8a` native ABI assumption unless the task
  explicitly changes device support.
- Avoid committing generated build output, downloaded model binaries, IDE state,
  or local machine configuration.

## ML And Model Notes

Model metadata lives in `models/models.manifest.json` and is the source of truth
for downloads and device pushes.

The embedding pipeline currently expects the E5-style contract:

- Prefix query text with `query: `.
- Prefix passage text with `passage: `.
- Mean-pool token embeddings.
- L2-normalize vectors.
- Keep tokenizer max length at `512` unless the model contract changes.

Relevant files:

- `app/src/main/java/il/nfm/localmind/ml/EmbeddingModel.kt`
- `app/src/main/java/il/nfm/localmind/ml/Tokenizer.kt`

## Checks Before Finishing

For Kotlin or Android changes, run at least:

```bash
./gradlew ktlintCheck detekt test
```

For native, Gradle, dependency, or app wiring changes, also run:

```bash
./gradlew assembleDebug
```

If a check cannot be run because local Android tooling, Rust targets, model
files, network access, or device access are unavailable, mention that clearly in
the final response.

## Git Hooks And Commits

The repository uses `lefthook.yml`.

Pre-commit runs:

- `./gradlew ktlintFormat`
- `./gradlew ktlintCheck`
- `./gradlew detekt`

Commit messages are validated by `scripts/validate-commit-msg.sh`; use
Conventional Commits style, for example `feat: add note index screen` or
`fix: handle missing tokenizer file`.
