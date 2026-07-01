# Spec: Model Coexistence Probe

## Status
Finished

## Goal
Prove that LLM and embedding models can be loaded together and each run on a
Nothing Phone 2A without the app being killed.

## Context
The biggest technical risk in the project is the LLM and embedding model running simultaneously.

## Probe Boundary
This spec answers whether the LLM and embedder can coexist in memory and perform
their minimal independent workloads in one app session. It does not establish
the retrieval architecture or the quality of RAG over notes.

The finished implementation also exercised a tiny fixed-note retrieval flow to
force the models to remain resident through a realistic sequence. That result is
useful evidence for this probe, but the RAG behavior itself belongs to
`docs/specs/002-hardcoded-notes-rag.md`.

## Non-Goals
- User-friendly UI
- Clean Architecture
- Persistence
- Vector DB
- Background indexing
- Citations
- Streaming polish
- Production prompt quality
- Real note CRUD

## User / Developer Flow
1. Developer launches the probe app on the physical target device.
2. The app loads the LLM, embedding model, and tokenizer.
3. The app embeds a hardcoded text while the LLM remains resident.
4. The app generates from a hardcoded prompt while the embedder remains resident.
5. Developer records load time, runtime latency, throughput, and memory.

## Requirements
- LLM model loads and runs on device
- Embedding model loads and runs on device
- Both models remain resident while the app embeds text and generates an answer
- The probe can be repeated with different LLM/embedder combinations without
  changing the result schema

## Data / State
Use only hardcoded probe inputs. No notes, persistence, or reusable app state are
required for this spec.

## Test Matrix
Record each model combination in `docs/model-coexistence-results.csv`.

Each row represents one physical-device run for one LLM/embedder/tokenizer
combination. If a model cannot load, still record the row with `outcome=fail`
and fill in the failure notes.

## UX Notes
Simplest UX: a single screen that runs the probe and shows pass/fail plus the
recorded metrics.

## Technical Notes
- LLM: models/llm/gemma-4-E2B-it.litertlm
- Embedder: models/embedding/multilingual-e5-small.onnx
- Tokenizer: models/embedding/tokenizer.json
- LLM Runtime: LiteRT-LM
- Embedder Runtime: ONNX Runtime
- E5 prefixes: `query: ` and `passage: `
- Mean pooling + L2 normalization
- Tokenizer max length: 512
- ABI remains `arm64-v8a`

## Metrics
- LLM load time
- Embedder load time
- Tokenizer load time
- First query embedding latency
- Hardcoded text embedding latency
- Time to first token
- Tokens/sec
- Peak RSS / PSS with both models loaded
- Whether app was killed, froze, or hit OOM

## Acceptance Criteria
- App does not crash or get killed with both models resident.
- Both models can run sequentially in one app session.
- Peak memory is recorded and considered survivable on the target device.
- Metrics are recorded in the Results section below and in
  `docs/model-coexistence-results.csv`.

## Results
- Device: Nothing A142
- Android version: 16
- App commit: 7ec2656-dirty
- LLM model: models/llm/gemma-4-E2B-it.litertlm
- Embedder model: models/embedding/multilingual-e5-small.onnx
- Tokenizer: models/embedding/tokenizer.json
- LLM load time: 9512 ms
- Embedder load time: 5388 ms
- Tokenizer load time: 3498 ms
- Peak memory: 3388 MB PSS / 3440 MB RSS with both models loaded
- TTFT: 1704 ms
- Tokens/sec: 5.20
- Query embedding latency: 473 ms
- Passage embedding latency for 10 notes: 4794 ms
- Retrieval latency: 2 ms
- Outcome: pass; retrieved expected note and generated answer used retrieved note.

Note: this run used the fixed-note RAG smoke path that later became
`docs/specs/002-hardcoded-notes-rag.md`. For Phase 0, the relevant result is
that the app survived with both models resident and both model runtimes executed
in one foreground session.

## Generated Test Expectations
An implementation agent should generate focused tests or smoke checks for:

- Model manifest entries resolve to the expected local and device paths.
- The embedder applies the E5 `query: ` and `passage: ` prefixes.
- Embeddings are mean-pooled and L2-normalized.
- The fixed-note retrieval path returns the expected note for at least one known
  smoke case if the implementation still uses that path to exercise both models.
- Metrics logging emits every field required by
  `docs/model-coexistence-results.csv`.

## Open Questions
- Can the embedder and LLM model run at the same time on the test device?
  Yes. The physical-device probe completed with both models resident during
  passage embedding, query embedding, retrieval, and generation.

## Test Device
- Device: Nothing Phone 2A
- RAM: 12 GB
- Android version: 16
- Build type: debug

## Verification
- [x] `./gradlew ktlintCheck`
- [x] `./gradlew detekt`
- [x] `./gradlew test`
- [x] `./gradlew assembleDebug` if native/Gradle/app wiring changed
- [x] Physical device smoke test if behavior depends on model/device runtime
