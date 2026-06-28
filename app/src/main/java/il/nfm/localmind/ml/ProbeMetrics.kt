package il.nfm.localmind.ml

import android.os.Build
import android.os.Debug
import java.io.File
import java.time.Instant
import java.util.Locale

data class ProbeMemorySnapshot(
    val pssMb: Long,
    val rssMb: Long,
)

object ProbeMemory {
    fun snapshot(): ProbeMemorySnapshot {
        val memoryInfo = Debug.MemoryInfo()
        Debug.getMemoryInfo(memoryInfo)
        return ProbeMemorySnapshot(
            pssMb = memoryInfo.totalPss.toLong() / KILOBYTES_PER_MEGABYTE,
            rssMb = readRssKb() / KILOBYTES_PER_MEGABYTE,
        )
    }

    private fun readRssKb(): Long =
        runCatching {
            File("/proc/self/status")
                .readLines()
                .first { it.startsWith("VmRSS:") }
                .split(Regex("\\s+"))[1]
                .toLong()
        }.getOrDefault(0L)

    private const val KILOBYTES_PER_MEGABYTE = 1024L
}

data class ProbeMetrics(
    val runId: String = Instant.now().toString(),
    val date: String = Instant.now().toString(),
    val appCommit: String = "manual-run",
    val device: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val androidVersion: String = Build.VERSION.RELEASE,
    val ramGb: String = "unknown",
    val buildType: String = "debug",
    val llmModel: String = ModelDevicePaths.LLM,
    val llmRuntime: String = "LiteRT-LM",
    val embedderModel: String = ModelDevicePaths.EMBEDDER,
    val embedderRuntime: String = "ONNX Runtime",
    val tokenizer: String = ModelDevicePaths.EMBEDDER_TOKENIZER,
    val notesCount: Int = ProbeNotes.notes.size,
    val llmLoadMs: Long = 0L,
    val embedderLoadMs: Long = 0L,
    val tokenizerLoadMs: Long = 0L,
    val queryEmbeddingMs: Long = 0L,
    val passageEmbedding10NotesMs: Long = 0L,
    val retrievalMs: Long = 0L,
    val ttftMs: Long = 0L,
    val tokensPerSec: Double = 0.0,
    val pssLlmOnlyMb: Long = 0L,
    val pssEmbedderOnlyMb: Long = 0L,
    val pssBothLoadedMb: Long = 0L,
    val rssBothLoadedMb: Long = 0L,
    val retrievedExpectedNote: Boolean = false,
    val answerUsesRetrievedNote: Boolean = false,
    val appKilled: Boolean = false,
    val appFroze: Boolean = false,
    val oom: Boolean = false,
    val outcome: String = "unknown",
    val notes: String = "",
) {
    fun toCsvRow(): String =
        csvFields()
            .joinToString(",") { it.csvEscape() }

    fun summary(): String =
        """
        CSV row:
        ${toCsvRow()}

        llm_load_ms=$llmLoadMs
        embedder_load_ms=$embedderLoadMs
        tokenizer_load_ms=$tokenizerLoadMs
        query_embedding_ms=$queryEmbeddingMs
        passage_embedding_10_notes_ms=$passageEmbedding10NotesMs
        retrieval_ms=$retrievalMs
        ttft_ms=$ttftMs
        tokens_per_sec=${"%.2f".format(Locale.US, tokensPerSec)}
        pss_both_loaded_mb=$pssBothLoadedMb
        rss_both_loaded_mb=$rssBothLoadedMb
        retrieved_expected_note=$retrievedExpectedNote
        answer_uses_retrieved_note=$answerUsesRetrievedNote
        outcome=$outcome
        """.trimIndent()

    private fun csvFields(): List<String> =
        listOf(
            runId,
            date,
            appCommit,
            device,
            androidVersion,
            ramGb,
            buildType,
            llmModel,
            llmRuntime,
            embedderModel,
            embedderRuntime,
            tokenizer,
            notesCount.toString(),
            llmLoadMs.toString(),
            embedderLoadMs.toString(),
            tokenizerLoadMs.toString(),
            queryEmbeddingMs.toString(),
            passageEmbedding10NotesMs.toString(),
            retrievalMs.toString(),
            ttftMs.toString(),
            "%.2f".format(Locale.US, tokensPerSec),
            pssLlmOnlyMb.toString(),
            pssEmbedderOnlyMb.toString(),
            pssBothLoadedMb.toString(),
            rssBothLoadedMb.toString(),
            retrievedExpectedNote.toString(),
            answerUsesRetrievedNote.toString(),
            appKilled.toString(),
            appFroze.toString(),
            oom.toString(),
            outcome,
            notes,
        )

    private fun String.csvEscape(): String =
        if (contains(',') || contains('"') || contains('\n')) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }
}

object ProbeMetricsCsv {
    val header =
        listOf(
            "run_id",
            "date",
            "app_commit",
            "device",
            "android_version",
            "ram_gb",
            "build_type",
            "llm_model",
            "llm_runtime",
            "embedder_model",
            "embedder_runtime",
            "tokenizer",
            "notes_count",
            "llm_load_ms",
            "embedder_load_ms",
            "tokenizer_load_ms",
            "query_embedding_ms",
            "passage_embedding_10_notes_ms",
            "retrieval_ms",
            "ttft_ms",
            "tokens_per_sec",
            "pss_llm_only_mb",
            "pss_embedder_only_mb",
            "pss_both_loaded_mb",
            "rss_both_loaded_mb",
            "retrieved_expected_note",
            "answer_uses_retrieved_note",
            "app_killed",
            "app_froze",
            "oom",
            "outcome",
            "notes",
        )
}
