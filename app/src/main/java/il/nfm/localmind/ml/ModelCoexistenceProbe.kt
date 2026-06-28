package il.nfm.localmind.ml

import android.os.SystemClock
import android.util.Log

class ModelCoexistenceProbe(
    private val llmEngine: LLMEngine,
    private val embedder: Embedder,
) {
    @Suppress("TooGenericExceptionCaught")
    suspend fun run(
        query: String,
        onToken: (String) -> Unit,
    ): ProbeRunResult {
        var llmLoadMs = 0L
        var embedderLoadMetrics = EmbedderLoadMetrics(embedderLoadMs = 0L, tokenizerLoadMs = 0L)
        var pssLlmOnlyMb = 0L
        var pssBothLoadedMb = 0L
        var rssBothLoadedMb = 0L

        return try {
            llmLoadMs = llmEngine.initialize()
            pssLlmOnlyMb = ProbeMemory.snapshot().pssMb

            embedderLoadMetrics = embedder.initialize()
            val bothLoadedMemory = ProbeMemory.snapshot()
            pssBothLoadedMb = bothLoadedMemory.pssMb
            rssBothLoadedMb = bothLoadedMemory.rssMb

            val passageEmbedding =
                timed {
                    ProbeNotes.notes.map { note ->
                        EmbeddedProbeNote(note = note, vector = embedder.embedPassage(note.content))
                    }
                }
            val queryEmbedding = timed { embedder.embedQuery(query) }
            val retrieval = timed { retrieveTopProbeNote(queryEmbedding.value, passageEmbedding.value) }
            val generated = generate(query = query, retrievedNote = retrieval.value, onToken = onToken)
            val expectedNoteId = ProbeNotes.expectedNoteIdFor(query)
            val retrievedExpectedNote = expectedNoteId == null || expectedNoteId == retrieval.value.id
            val answerUsesRetrievedNote = generated.answer.usesContentFrom(retrieval.value)

            val metrics =
                ProbeMetrics(
                    llmLoadMs = llmLoadMs,
                    embedderLoadMs = embedderLoadMetrics.embedderLoadMs,
                    tokenizerLoadMs = embedderLoadMetrics.tokenizerLoadMs,
                    queryEmbeddingMs = queryEmbedding.elapsedMs,
                    passageEmbedding10NotesMs = passageEmbedding.elapsedMs,
                    retrievalMs = retrieval.elapsedMs,
                    ttftMs = generated.ttftMs,
                    tokensPerSec = generated.tokensPerSec,
                    pssLlmOnlyMb = pssLlmOnlyMb,
                    pssEmbedderOnlyMb = 0L,
                    pssBothLoadedMb = pssBothLoadedMb,
                    rssBothLoadedMb = rssBothLoadedMb,
                    retrievedExpectedNote = retrievedExpectedNote,
                    answerUsesRetrievedNote = answerUsesRetrievedNote,
                    outcome = if (retrievedExpectedNote && answerUsesRetrievedNote) "pass" else "fail",
                    notes = "pss_embedder_only_mb not isolated; LLM intentionally stayed resident before embedding",
                )
            Log.i(LOG_TAG, metrics.toCsvRow())
            ProbeRunResult(
                retrievedNote = retrieval.value,
                answer = generated.answer,
                metrics = metrics,
            )
        } catch (oom: OutOfMemoryError) {
            failureResult(
                query = query,
                llmLoadMs = llmLoadMs,
                embedderLoadMetrics = embedderLoadMetrics,
                pssLlmOnlyMb = pssLlmOnlyMb,
                pssBothLoadedMb = pssBothLoadedMb,
                rssBothLoadedMb = rssBothLoadedMb,
                oom = true,
                failure = oom,
            )
        } catch (failure: Exception) {
            failureResult(
                query = query,
                llmLoadMs = llmLoadMs,
                embedderLoadMetrics = embedderLoadMetrics,
                pssLlmOnlyMb = pssLlmOnlyMb,
                pssBothLoadedMb = pssBothLoadedMb,
                rssBothLoadedMb = rssBothLoadedMb,
                failure = failure,
            )
        }
    }

    private suspend fun generate(
        query: String,
        retrievedNote: ProbeNote,
        onToken: (String) -> Unit,
    ): GeneratedAnswer {
        val prompt = buildPrompt(query, listOf(retrievedNote.content))
        val startMs = SystemClock.elapsedRealtime()
        var firstTokenMs = 0L
        var tokenCount = 0
        val answer = StringBuilder()

        llmEngine.askOnce(prompt).collect { token ->
            if (tokenCount == 0) firstTokenMs = SystemClock.elapsedRealtime() - startMs
            tokenCount++
            answer.append(token)
            onToken(token)
        }

        val elapsedMs = (SystemClock.elapsedRealtime() - startMs).coerceAtLeast(1L)
        return GeneratedAnswer(
            answer = answer.toString(),
            ttftMs = firstTokenMs,
            tokensPerSec = tokenCount * MILLIS_PER_SECOND.toDouble() / elapsedMs,
        )
    }

    private fun failureResult(
        query: String,
        llmLoadMs: Long,
        embedderLoadMetrics: EmbedderLoadMetrics,
        pssLlmOnlyMb: Long,
        pssBothLoadedMb: Long,
        rssBothLoadedMb: Long,
        oom: Boolean = false,
        failure: Throwable,
    ): ProbeRunResult {
        val note = ProbeNotes.notes.first()
        val metrics =
            ProbeMetrics(
                llmLoadMs = llmLoadMs,
                embedderLoadMs = embedderLoadMetrics.embedderLoadMs,
                tokenizerLoadMs = embedderLoadMetrics.tokenizerLoadMs,
                pssLlmOnlyMb = pssLlmOnlyMb,
                pssEmbedderOnlyMb = 0L,
                pssBothLoadedMb = pssBothLoadedMb,
                rssBothLoadedMb = rssBothLoadedMb,
                retrievedExpectedNote = false,
                answerUsesRetrievedNote = false,
                oom = oom,
                outcome = "fail",
                notes = "query=$query; failure=${failure::class.simpleName}: ${failure.message.orEmpty()}",
            )
        Log.e(LOG_TAG, metrics.toCsvRow(), failure)
        return ProbeRunResult(retrievedNote = note, answer = "", metrics = metrics)
    }

    private suspend fun <T> timed(block: suspend () -> T): Timed<T> {
        val startMs = SystemClock.elapsedRealtime()
        val value = block()
        return Timed(value = value, elapsedMs = SystemClock.elapsedRealtime() - startMs)
    }

    private data class Timed<T>(
        val value: T,
        val elapsedMs: Long,
    )

    private data class GeneratedAnswer(
        val answer: String,
        val ttftMs: Long,
        val tokensPerSec: Double,
    )

    private companion object {
        const val LOG_TAG = "ModelCoexistenceProbe"
        const val MILLIS_PER_SECOND = 1000L
    }
}

data class ProbeRunResult(
    val retrievedNote: ProbeNote,
    val answer: String,
    val metrics: ProbeMetrics,
)

private fun String.usesContentFrom(note: ProbeNote): Boolean {
    val answer = lowercase()
    return note.content
        .lowercase()
        .split(Regex("[^a-z0-9:.'-]+"))
        .filter { it.length >= MIN_MATCH_WORD_LENGTH }
        .any { it in answer }
}

private const val MIN_MATCH_WORD_LENGTH = 5
