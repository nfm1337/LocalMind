# LocalMind

LocalMind is an Android prototype for on-device RAG over personal notes. The
project is currently focused on proving that the generation model and embedding
model can run together on a mid-range Android device before adding the full notes
and retrieval layers.

For the product and architecture direction, see:

- [docs/plan.md](docs/plan.md)
- [docs/architecture.md](docs/architecture.md)

## Project Layout

```text
.
├── app/                    Android application module
├── build-logic/            Local Gradle convention plugins
├── config/detekt/          Detekt configuration
├── docs/                   Product/architecture notes
├── models/                 Local model manifest and downloaded model files
├── scripts/                Model download/push scripts
├── gradle/libs.versions.toml
└── lefthook.yml            Git hook configuration
```

The app module is intentionally small at this stage. `app/src/main/java` contains
the Android/Compose app and ML wrappers. `app/src/main/cpp` contains the native
tokenizer bridge.

## Android Configuration

The Android module is configured in [app/build.gradle.kts](app/build.gradle.kts).
Shared Android defaults live in
[build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt](build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt).

Current Android settings:

- namespace/application id: `il.nfm.localmind`
- compile SDK: `36`
- min SDK: `31`
- target SDK: `36`
- Java compatibility: `11`
- ABI filter: `arm64-v8a`
- UI: Jetpack Compose + Material 3
- native build: CMake via `app/src/main/cpp/CMakeLists.txt`

ML runtime dependencies:

- `com.google.ai.edge.litertlm:litertlm-android` for the LLM
- `com.microsoft.onnxruntime:onnxruntime-android` for the embedder

Dependency versions are centralized in
[gradle/libs.versions.toml](gradle/libs.versions.toml).

## Models

Model metadata is defined in [models/models.manifest.json](models/models.manifest.json).
The manifest is the source of truth for model downloads and device pushes.

Each model entry contains:

- `fileName`: display/name of the file
- `localPath`: where the file should exist on the development machine
- `devicePath`: where the file should be placed on the Android device, relative to
  the model device root unless it starts with `/`
- `url`: download URL
- `sha256`: expected SHA-256 hash

The current runtime files are:

- LLM: `models/llm/gemma-4-E2B-it.litertlm`
- Embedder ONNX: `models/embedding/multilingual-e5-small.onnx`
- Embedder tokenizer: `models/embedding/tokenizer.json`

The embedder code expects the E5-style contract:

- query text is prefixed with `query: `
- passage text is prefixed with `passage: `
- embeddings are mean-pooled over the token sequence
- vectors are L2-normalized
- tokenizer max length is `512`

Those details are currently implemented in
[app/src/main/java/il/nfm/localmind/ml/EmbeddingModel.kt](app/src/main/java/il/nfm/localmind/ml/EmbeddingModel.kt)
and [app/src/main/java/il/nfm/localmind/ml/Tokenizer.kt](app/src/main/java/il/nfm/localmind/ml/Tokenizer.kt).

## Model Scripts

Gradle exposes model tasks from the root [build.gradle.kts](build.gradle.kts):

```bash
./gradlew downloadModels
./gradlew pushModels
./gradlew downloadAndPushModels
```

On Windows, the same tasks run the PowerShell scripts. On macOS/Linux, they run
the shell scripts.

The scripts are:

- [scripts/download-models.sh](scripts/download-models.sh)
- [scripts/download-models.ps1](scripts/download-models.ps1)
- [scripts/push-models.sh](scripts/push-models.sh)
- [scripts/push-models.ps1](scripts/push-models.ps1)

Shared script logic lives in:

- [scripts/lib/models.sh](scripts/lib/models.sh)
- [scripts/lib/models.ps1](scripts/lib/models.ps1)

The shared logic reads the manifest, expands the model entries, computes SHA-256
hashes, validates required fields, and verifies local model files.

### Download Behavior

`downloadModels` reads the manifest and handles each file like this:

1. If the local file exists and its SHA-256 matches the manifest, it is skipped.
2. If the file is missing or has the wrong hash, it is downloaded to a temporary
   `*.download` file.
3. The downloaded file hash is checked.
4. If the hash matches, the temporary file replaces the target file.
5. If the hash does not match, the temporary file is deleted and the script fails.

The macOS/Linux script requires either `jq` or `python3` to parse JSON. The
PowerShell script uses `ConvertFrom-Json`.

### Push Behavior

`pushModels` verifies local files before pushing:

1. The local file must exist.
2. Its SHA-256 must match the manifest.
3. The existing app-sandbox copy is removed to free space, then the file is
   streamed into the debuggable app's internal `files/` directory with
   `adb shell -T run-as`.
4. The pushed file is verified from the app sandbox using `sha256sum`, with
   fallback to `toybox sha256sum`.

Relative `devicePath` values are pushed under the debug app's internal files
directory:

```text
/data/user/0/il.nfm.localmind/files
```

Install the debug app before pushing models so `run-as il.nfm.localmind` is
available:

```bash
./gradlew installDebug
./gradlew pushModels
```

Override the package or device root when needed:

```bash
MODEL_APP_PACKAGE=il.nfm.localmind ./gradlew pushModels
MODEL_DEVICE_ROOT=/sdcard/Download/LocalMindModels ./gradlew pushModels
```

PowerShell:

```powershell
$env:MODEL_APP_PACKAGE = "il.nfm.localmind"
$env:MODEL_DEVICE_ROOT = "/sdcard/Download/LocalMindModels"
./gradlew pushModels
```

`adb` must be installed, on `PATH`, and connected to a device.

## Native Toolchain

The app builds a native tokenizer bridge from `app/src/main/cpp`. The CMake build
fetches `mlc-ai/tokenizers-cpp`, which may compile Rust dependencies while
building the Android native target.

Required local tools:

- Android SDK
- Android NDK
- CMake, installed through the Android SDK manager
- Rust toolchain with `rustup`
- Rust Android target: `aarch64-linux-android`

Install the Rust Android target:

```bash
rustup target add aarch64-linux-android
```

This is required because the app currently builds only the `arm64-v8a` Android ABI.
If the target is missing, the native build can fail with:

```text
error[E0463]: can't find crate for `core`
= note: the `aarch64-linux-android` target may not be installed
```

After installing the target, rebuild:

```bash
./gradlew assembleDebug
```

## Build And Checks

Common commands:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew ktlintCheck
./gradlew ktlintFormat
./gradlew detekt
```

The project uses local convention plugins:

- `convention.android.app`: Android app defaults
- `convention.ktlint`: ktlint plus Compose rules
- `convention.detekt`: Detekt with [config/detekt/detekt.yml](config/detekt/detekt.yml)

Gradle configuration cache is enabled in [gradle.properties](gradle.properties).

## Git Hooks

The repository includes [lefthook.yml](lefthook.yml). Install Lefthook locally and
then install the hooks:

```bash
brew install lefthook
lefthook install
```

The configured pre-commit hook runs:

- `./gradlew ktlintFormat`
- `./gradlew ktlintCheck`
- `./gradlew detekt`

The commit message hook calls:

```text
scripts/validate-commit-msg.sh
```

## Development Notes

The current phase is still model/runtime validation, not a complete note-taking
application. The important setup path is:

```bash
./gradlew downloadModels
./gradlew pushModels
./gradlew assembleDebug
```

If a model download starts failing, update the URL and SHA-256 in
[models/models.manifest.json](models/models.manifest.json). Avoid changing script
constants for individual model files; the scripts are intentionally driven by the
manifest.
