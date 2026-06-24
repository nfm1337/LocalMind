# Phase 0 — Get embedder + LLM running and measured (on real device)

Goal: prove both models load, run, produce correct output, and CO-EXIST in RAM on
a real mid-range device. Throwaway code — no architecture, no modules, one Activity.
The only deliverables are: it works, and the numbers (tokens/sec, embed latency,
peak RAM with both resident).

> Note on emulator: ML perf/RAM numbers from an emulator are not representative
> (x86_64, no real GPU/NPU, PC RAM). Measure on the real device. Use the emulator
> only for non-ML UI iteration later.

---

## Step 0 — Project + device setup

```text
[x] New empty Compose project, min SDK ~26+, single MainActivity
[x] build.gradle: ndk { abiFilters += "arm64-v8a" }   (real device target)
[x] Enable USB debugging on the phone, confirm `adb devices` sees it
[x] Decide model delivery for dev: adb push to /data/local/tmp/ (no APK rebuild
    when swapping models) OR app/src/main/assets (simpler, but rebuild to swap)
[x] Add a tiny on-screen log area (Text in a scrollable Column) to print results
```

Recommendation: `adb push` models to `/data/local/tmp/` for Phase 0 — lets you
swap model files / quantizations without rebuilding the APK.

```text
adb push gemma-4-E2B-it.litertlm /data/local/tmp/
adb push embeddinggemma-300M_seq512_mixed-precision.tflite /data/local/tmp/
adb push sentencepiece.model /data/local/tmp/
```

---

## Step 1 — LLM generation (do this FIRST; it's the bigger risk)

Dependencies:
```text
implementation("com.google.ai.edge.litertlm:litertlm-android:<latest>")
```

Manifest (for GPU backend), inside <application>:
```text
<uses-native-library android:name="libvndksupport.so" android:required="false"/>
<uses-native-library android:name="libOpenCL.so" android:required="false"/>
```

Implement:
```text
[ ] Load chat_template format from the model repo (prompt prefix/suffix) —
    wrong template => garbage output even when "working"
[ ] Init Engine with model path on a BACKGROUND coroutine
    (init can take ~10s; blocking UI will ANR)
[ ] Create a Conversation, send one hardcoded prompt
    e.g. "Summarize: I bought milk and paid taxes on Tuesday."
[ ] Stream/collect the response, print to the on-screen log
[ ] Measure: time to first token, tokens/sec, peak RAM (see Step 4)
```

Exit check:
```text
[ ] A coherent answer appears (not gibberish -> template is correct)
[ ] tokens/sec recorded; cold init time recorded
```

---

## Step 2 — Embedder (LiteRT + SentencePiece)

Dependencies:
```text
implementation("com.google.ai.edge.litert:litert:<latest>")        // CompiledModel API
implementation("ai.djl.sentencepiece:sentencepiece:<latest>")      // tokenizer
```

Implement in small, separately-verified pieces:
```text
[ ] 2a. Tokenizer alive: load sentencepiece.model, encode("hello") -> int IDs,
        decode back -> "hello". Print IDs.
[ ] 2b. Golden test: tokenize the SAME string in Python (official EmbeddingGemma
        example) and compare the IDs. They MUST match. This is the silent-failure
        guard — wrong tokenization compiles fine but yields garbage vectors.
[ ] 2c. Add special tokens / task prefix per EmbeddingGemma model card
        (query vs document prefixes differ — affects retrieval quality)
[ ] 2d. Pad/truncate to seq length (512). Build attention mask if the model
        signature has a second input.
[ ] 2e. CompiledModel: createInputBuffers / createOutputBuffers, write int token
        IDs into input[0] (+ mask into input[1] if present), run(),
        read FloatArray out of output[0] -> this is the embedding
```

Exit check:
```text
[ ] embed("cat") and embed("dog") have HIGH cosine
[ ] embed("cat") and embed("taxes") have LOW cosine
[ ] embed query-prefixed vs doc-prefixed behaves sanely
[ ] embedding latency recorded
```

---

## Step 3 — The real Phase 0 question: both models in RAM together

```text
[ ] Load the LLM engine AND the embedder in the same app session
[ ] Run one embed, then one generation, then one embed again
    (proves neither load corrupts/evicts the other)
[ ] Watch for: OOM / the OS killing the app / massive slowdown
```

Exit check:
```text
[ ] Both resident simultaneously; app survives; peak RAM is known and acceptable
[ ] If it does NOT fit: fall back (smaller LLM quant, or load/unload around ops)
    — and record that as the key finding
```

---

## Step 4 — How to measure RAM and latency

```text
RAM:
[ ] adb shell dumpsys meminfo <package> (look at TOTAL PSS) after each model loads
[ ] or Android Studio Profiler (Memory) during the session
[ ] record: PSS after LLM only, after embedder only, after BOTH

Latency:
[ ] System.nanoTime() around: engine init, single embed, generation
[ ] tokens/sec = generated_tokens / generation_seconds
```

---

## Step 5 — (optional) emulator check for build sanity only

```text
[ ] If you want to run on emulator at all, add "x86_64" to abiFilters and confirm
    all native libs (LiteRT, LiteRT-LM, DJL, later ObjectBox) ship x86_64 .so
[ ] Use it ONLY to confirm the app builds/links on a second ABI — NOT for perf/RAM
```

---

## Phase 0 done when

```text
[ ] LLM generates coherent text on the real device (correct chat template)
[ ] Embedder produces sane vectors (golden-tested tokenization, cosine sanity)
[ ] Both models co-exist in RAM; peak RAM known and survivable
[ ] Recorded numbers: cold init, tokens/sec, embed latency, peak RAM
[ ] Decision: proceed as-is, or adjust model/quant based on the numbers
```

All of this is throwaway. Do not build modules, DI, or abstractions here. The next
phase (vertical RAG slice) starts clean and reuses only what you learned, not this code.