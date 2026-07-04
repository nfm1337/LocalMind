# LocalMind Architecture

## Current MVP Architecture

LocalMind uses Room as the local source of truth and embedding cache.

For MVP, retrieval is implemented as brute-force cosine similarity over note-level embeddings loaded from Room.

## Data Flow

Create/Edit note
→ Save note in Room
→ Generate note embedding
→ Store embedding in Room
→ Embed query
→ Compare query vector with stored note vectors
→ Select top-K notes
→ Build prompt
→ Generate answer
→ Show answer with source notes

## Key Decisions

### Room first

Room is used because it is standard Android infrastructure, easy to test, easy to explain, and enough for MVP-scale local data.

### No ObjectBox for MVP

ObjectBox/vector index is deferred because it adds dependency and complexity before there is evidence that brute-force search is insufficient.

### Note-level embeddings first

One note = one embedding.

Chunking is deferred until realistic notes show quality problems.

### Brute-force search first

Brute-force cosine search is acceptable for MVP because the expected local dataset is small.

### Sources before precise citations

MVP answers should show source notes. Precise span highlighting is deferred.

## Deferred

- Background indexing
- Dirty state
- FTS fallback
- Chunk-level embeddings
- Vector database
- PDF/OCR