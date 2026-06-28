# Spec: Model Coexistence Probe

## Status
Finished

## Goal
Prove that LLM and embedding models can be loaded together and used on a Nothing Phone 2A.

## Context
The biggest technical risk in the project is the LLM and embedding model running simultaneously.

## Probe Boundary
This spec answers whether the LLM and embedder can coexist in memory and run in
one foreground question-answer flow over fixed notes. It does not establish the
final retrieval architecture.

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
1. User submits a question
2. The embedding model embeds the question and finds a note related to it
3. The LLM generates an answer from the retrieved note
4. User sees the generated answer

## Requirements
- LLM model loads and runs on device
- Embedding model loads and runs on device
- Both models remain resident while the app embeds the query, retrieves from the
  fixed notes, and generates an answer
- The probe can be repeated with different LLM/embedder combinations without
  changing the result schema

## Data / State
Use 10 preloaded notes and operate only on them.

The implementation may keep these notes in code for this probe. Use stable IDs so
retrieval tests can assert the expected result.

| ID               | Note                                                                                       |
|------------------|--------------------------------------------------------------------------------------------|
| `note_groceries` | Bought milk, eggs, tomatoes, and coffee from the neighborhood grocery store.               |
| `note_tax`       | Paid quarterly taxes on Tuesday and saved the confirmation number in email.                |
| `note_flight`    | Flight to Berlin leaves Friday morning from terminal 3.                                    |
| `note_book`      | Recommended book: The Design of Everyday Things, especially the chapter about affordances. |
| `note_gym`       | Gym session focused on squats, rowing, and stretching.                                     |
| `note_dentist`   | Dentist appointment is scheduled for next Monday at 09:30.                                 |
| `note_project`   | LocalMind needs the LLM and embedding model to fit in memory on a mid-range phone.         |
| `note_recipe`    | Pasta sauce recipe uses tomatoes, garlic, olive oil, basil, and parmesan.                  |
| `note_birthday`  | Maya's birthday gift idea: noise-canceling headphones.                                     |
| `note_wifi`      | Home Wi-Fi router admin page is at 192.168.1.1.                                            |

## Probe Cases

| Query                                               | Expected note ID |
|-----------------------------------------------------|------------------|
| What do I need to prove for LocalMind on the phone? | `note_project`   |
| When is my dentist appointment?                     | `note_dentist`   |
| What should I buy for Maya?                         | `note_birthday`  |

## Test Matrix
Record each model combination in `docs/model-coexistence-results.csv`.

Each row represents one physical-device run for one LLM/embedder/tokenizer
combination. If a model cannot load, still record the row with `outcome=fail`
and fill in the failure notes.

## UX Notes
Simplest UX - text field for input and button to submit.

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
- Passage embedding latency for 10 notes
- Retrieval latency
- Time to first token
- Tokens/sec
- Peak RSS / PSS with both models loaded
- Whether app was killed, froze, or hit OOM

## Acceptance Criteria
- App does not crash or get killed with both models resident.
- Both models can run sequentially in one user flow.
- Peak memory is recorded and considered survivable on the target device.
- A question over 10 fixed notes retrieves the expected note.
- The generated answer uses content from the retrieved note.
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

## Generated Test Expectations
An implementation agent should generate focused tests or smoke checks for:

- Model manifest entries resolve to the expected local and device paths.
- The embedder applies the E5 `query: ` and `passage: ` prefixes.
- Embeddings are mean-pooled and L2-normalized.
- The fixed-note retrieval path returns the expected note for at least one known
  probe case.
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
