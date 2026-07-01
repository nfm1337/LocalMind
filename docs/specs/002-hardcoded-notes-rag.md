# Spec: Hardcoded Notes RAG

## Status
Draft

## Goal
Build the first end-to-end RAG vertical slice: a user asks a question, the app
retrieves from a tiny fixed set of notes, and the LLM answers using the retrieved
context.

## Context
This spec maps to Phase 1 in `docs/plan.md`. Phase 0 proved the LLM and embedder
can coexist on the target device. The next risk is whether the embedding model
retrieves the right short note for natural questions and whether that retrieved
note can be passed through the prompt into a grounded answer.

This is still a prototype slice. It should exercise the real model wrappers and
the full retrieve → prompt → generate path, without adding persistence,
ObjectBox, real note CRUD, background indexing, citations, or UI polish.

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
- Chunking
- Hybrid dirty-note handling
- FTS

## User / Developer Flow
1. User opens the single question screen.
2. The app embeds the fixed note set in memory at launch or before first query.
3. User types a question and submits it.
4. The app embeds the query, retrieves the top note or notes by cosine
   similarity, and builds a prompt from the question plus retrieved note context.
5. The LLM generates an answer.
6. User sees the answer and enough retrieval/debug information to confirm the
   answer used the expected note.

## Requirements
- Use the real embedding model wrapper and LLM runner used by the app.
- Keep the fixed notes in memory only.
- Compute passage embeddings for the fixed notes in memory.
- Prefix query text with `query: ` before embedding.
- Prefix note text with `passage: ` before embedding.
- Mean-pool token embeddings and L2-normalize vectors.
- Retrieve by brute-force cosine similarity over the fixed note vectors.
- Build the prompt from the user question and the retrieved note context.
- Generate an answer from the LLM using that prompt.
- Show the generated answer.
- Show the retrieved note ID or note title in debug UI or logs.
- Record retrieval and generation metrics for each physical-device run.

## Data / State
Use 10 preloaded notes and operate only on them.

The implementation may keep these notes in code for this slice. Use stable IDs so
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

The source of truth for this slice is the in-code note list. Embeddings are
derived in-memory data and may be recomputed on every app launch.

## Probe Cases

| Query                                               | Expected note ID |
|-----------------------------------------------------|------------------|
| What do I need to prove for LocalMind on the phone? | `note_project`   |
| When is my dentist appointment?                     | `note_dentist`   |
| What should I buy for Maya?                         | `note_birthday`  |
| What ingredients go into the pasta sauce?           | `note_recipe`    |
| Where is my router admin page?                      | `note_wifi`      |

## UX Notes
Simplest UX: one text field, one submit button, answer text, loading/error
states, and a visible retrieved note ID for debugging. No navigation or note
editing UI is required.

## Technical Notes
- LLM: `models/llm/gemma-4-E2B-it.litertlm`
- Embedder: `models/embedding/multilingual-e5-small.onnx`
- Tokenizer: `models/embedding/tokenizer.json`
- LLM Runtime: LiteRT-LM
- Embedder Runtime: ONNX Runtime
- Tokenizer max length: 512
- ABI remains `arm64-v8a`
- Retrieval can be a small pure Kotlin function over in-memory vectors.
- Do not introduce ObjectBox or a vector database in this slice.
- Do not hard-code model paths in app code; use the manifest/script conventions
  already documented in `README.md`.

## Metrics
- Passage embedding latency for 10 notes
- Query embedding latency
- Retrieval latency
- Time to first token
- Tokens/sec
- Peak RSS / PSS with both models loaded
- Retrieved note ID for each probe case
- Whether the generated answer uses content from the retrieved note

## Acceptance Criteria
- App builds and runs on the physical target device.
- Typing a question returns an answer.
- At least the listed probe cases retrieve the expected note.
- The generated answer uses content from the retrieved note.
- The retrieve → prompt → generate path works as one call chain.
- Metrics are recorded in `docs/model-coexistence-results.csv` or a successor
  results file if this slice gets its own run log.

## Risks
- The small embedding model may fail short natural-language queries even when
  the models coexist successfully.
- The retrieved note may be correct, but the LLM may ignore or distort the
  retrieved context.
- Passage embedding at startup may be slow enough to require a visible loading
  state even for this tiny fixed set.

## Open Questions
- Should Phase 1 reuse `docs/model-coexistence-results.csv`, or should it get a
  dedicated RAG results file?
- What minimum answer-grounding check is enough before moving to persistence:
  human smoke test, unit assertions over retrieval only, or both?

## Verification
- [ ] `./gradlew ktlintCheck`
- [ ] `./gradlew detekt`
- [ ] `./gradlew test`
- [ ] `./gradlew assembleDebug` if native/Gradle/app wiring changed
- [ ] Physical device smoke test if behavior depends on model/device runtime
