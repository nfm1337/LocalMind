package il.nfm.localmind

import android.os.Build
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import il.nfm.localmind.ml.DebugModelPathProvider
import il.nfm.localmind.ml.E5SmallEmbedder
import il.nfm.localmind.ml.Embedder
import il.nfm.localmind.ml.EmbedderLoadMetrics
import il.nfm.localmind.ml.LLMEngine
import il.nfm.localmind.ml.LiteRtLLMEngine
import il.nfm.localmind.ml.ModelDevicePaths
import il.nfm.localmind.ml.buildPrompt
import il.nfm.localmind.ml.dot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.Instant
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ModelCoexistenceSmokeTest {
    private var llmEngine: LLMEngine? = null
    private var embedder: Embedder? = null

    @After
    fun tearDown() {
        runBlocking {
            embedder?.close()
            llmEngine?.close()
        }
    }

    @Test
    fun physicalModelCoexistenceProbe() =
        runBlocking {
            val arguments = InstrumentationRegistry.getArguments()
            assumeTrue("Set physicalModelProbe=true to run the device model probe", arguments.optInEnabled())

            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val paths = DebugModelPathProvider(context)
            assumeTrue("Missing LLM model: ${paths.llmModelPath}", File(paths.llmModelPath).isFile)
            assumeTrue("Missing embedder model: ${paths.embeddingModelPath}", File(paths.embeddingModelPath).isFile)
            assumeTrue("Missing tokenizer: ${paths.embeddingTokenizerPath}", File(paths.embeddingTokenizerPath).isFile)

            llmEngine = LiteRtLLMEngine(paths.llmModelPath)
            embedder =
                E5SmallEmbedder(
                    modelPath = paths.embeddingModelPath,
                    tokenizerPath = paths.embeddingTokenizerPath,
                )

            val result =
                ModelProbeRunner(
                    llmEngine = checkNotNull(llmEngine),
                    embedder = checkNotNull(embedder),
                    appCommit = arguments.getString(ARG_APP_COMMIT) ?: "androidTest-run",
                ).run(ProbeNotes.cases)

            Log.i(LOG_TAG, result.metrics.toCsvRow())

            assertEquals(PROBE_CSV_FIELD_COUNT, result.metrics.fieldCount())
            assertEquals("note_project", result.retrievedNote.id)
            result.retrievalResults.forEach { retrieval ->
                assertEquals(retrieval.case.expectedNoteId, retrieval.retrievedNote.id)
            }
            assertEquals("pass", result.metrics.outcome)
            assertTrue(result.metrics.retrievedExpectedNote)
            assertTrue(result.metrics.answerUsesRetrievedNote)
            assertFalse(result.metrics.oom)
            assertFalse(result.metrics.appKilled)
            assertFalse(result.metrics.appFroze)
        }

    private fun android.os.Bundle.optInEnabled(): Boolean =
        getString(ARG_PHYSICAL_MODEL_PROBE).equals("true", ignoreCase = true)

    private class ModelProbeRunner(
        private val llmEngine: LLMEngine,
        private val embedder: Embedder,
        private val appCommit: String,
    ) {
        @Suppress("TooGenericExceptionCaught")
        suspend fun run(cases: List<ProbeCase>): ProbeRunResult {
            require(cases.isNotEmpty()) { "At least one probe case is required" }

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
                val retrievalResults =
                    cases.map { case ->
                        val queryEmbedding = timed { embedder.embedQuery(case.query) }
                        val retrieval = timed { retrieveTopProbeNote(queryEmbedding.value, passageEmbedding.value) }
                        ProbeRetrievalResult(
                            case = case,
                            retrievedNote = retrieval.value,
                            queryEmbeddingMs = queryEmbedding.elapsedMs,
                            retrievalMs = retrieval.elapsedMs,
                        )
                    }
                val primaryRetrieval = retrievalResults.first()
                val generated =
                    generate(
                        query = primaryRetrieval.case.query,
                        retrievedNote = primaryRetrieval.retrievedNote,
                    )
                val retrievedExpectedNote = retrievalResults.all { it.case.expectedNoteId == it.retrievedNote.id }
                val answerUsesRetrievedNote = generated.answer.usesContentFrom(primaryRetrieval.retrievedNote)

                ProbeRunResult(
                    retrievedNote = primaryRetrieval.retrievedNote,
                    retrievalResults = retrievalResults,
                    answer = generated.answer,
                    metrics =
                        ProbeMetrics(
                            appCommit = appCommit,
                            llmLoadMs = llmLoadMs,
                            embedderLoadMs = embedderLoadMetrics.embedderLoadMs,
                            tokenizerLoadMs = embedderLoadMetrics.tokenizerLoadMs,
                            queryEmbeddingMs = primaryRetrieval.queryEmbeddingMs,
                            passageEmbedding10NotesMs = passageEmbedding.elapsedMs,
                            retrievalMs = primaryRetrieval.retrievalMs,
                            ttftMs = generated.ttftMs,
                            tokensPerSec = generated.tokensPerSec,
                            pssLlmOnlyMb = pssLlmOnlyMb,
                            pssEmbedderOnlyMb = 0L,
                            pssBothLoadedMb = pssBothLoadedMb,
                            rssBothLoadedMb = rssBothLoadedMb,
                            retrievedExpectedNote = retrievedExpectedNote,
                            answerUsesRetrievedNote = answerUsesRetrievedNote,
                            outcome = if (retrievedExpectedNote && answerUsesRetrievedNote) "pass" else "fail",
                            notes =
                                "pss_embedder_only_mb not isolated; " +
                                    "LLM intentionally stayed resident before embedding; " +
                                    "retrieval_cases=${retrievalResults.size}",
                        ),
                )
            } catch (oom: OutOfMemoryError) {
                failureResult(
                    query = cases.first().query,
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
                    query = cases.first().query,
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
        ): ProbeRunResult =
            ProbeRunResult(
                retrievedNote = ProbeNotes.notes.first(),
                retrievalResults = emptyList(),
                answer = "",
                metrics =
                    ProbeMetrics(
                        appCommit = appCommit,
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
                    ),
            )

        private fun retrieveTopProbeNote(
            queryVector: FloatArray,
            index: List<EmbeddedProbeNote>,
        ): ProbeNote =
            checkNotNull(index.maxByOrNull { dot(queryVector, it.vector) }) {
                "Probe index is empty"
            }.note

        private fun String.usesContentFrom(note: ProbeNote): Boolean {
            val answer = lowercase()
            return note.content
                .lowercase()
                .split(Regex("[^a-z0-9:.'-]+"))
                .filter { it.length >= MIN_MATCH_WORD_LENGTH }
                .any { it in answer }
        }

        private suspend fun <T> timed(block: suspend () -> T): Timed<T> {
            val startMs = SystemClock.elapsedRealtime()
            val value = block()
            return Timed(value = value, elapsedMs = SystemClock.elapsedRealtime() - startMs)
        }
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

    private data class ProbeRunResult(
        val retrievedNote: ProbeNote,
        val retrievalResults: List<ProbeRetrievalResult>,
        val answer: String,
        val metrics: ProbeMetrics,
    )

    private data class ProbeNote(
        val id: String,
        val content: String,
    )

    private data class ProbeCase(
        val query: String,
        val expectedNoteId: String,
    )

    private data class ProbeRetrievalResult(
        val case: ProbeCase,
        val retrievedNote: ProbeNote,
        val queryEmbeddingMs: Long,
        val retrievalMs: Long,
    )

    private data class EmbeddedProbeNote(
        val note: ProbeNote,
        val vector: FloatArray,
    )

    private data class ProbeMemorySnapshot(
        val pssMb: Long,
        val rssMb: Long,
    )

    private data class ProbeMetrics(
        val runId: String = Instant.now().toString(),
        val date: String = Instant.now().toString(),
        val appCommit: String,
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
        fun fieldCount(): Int = csvFields().size

        fun toCsvRow(): String =
            csvFields()
                .joinToString(",") { it.csvEscape() }

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

    private object ProbeNotes {
        val notes =
            listOf(
                ProbeNote(
                    "note_groceries",
                    "Bought milk, eggs, tomatoes, and coffee from the neighborhood grocery store.",
                ),
                ProbeNote("note_tax", "Paid quarterly taxes on Tuesday and saved the confirmation number in email."),
                ProbeNote("note_flight", "Flight to Berlin leaves Friday morning from terminal 3."),
                ProbeNote(
                    "note_book",
                    "Recommended book: The Design of Everyday Things, especially the chapter about affordances.",
                ),
                ProbeNote("note_gym", "Gym session focused on squats, rowing, and stretching."),
                ProbeNote("note_dentist", "Dentist appointment is scheduled for next Monday at 09:30."),
                ProbeNote(
                    "note_project",
                    "LocalMind needs the LLM and embedding model to fit in memory on a mid-range phone.",
                ),
                ProbeNote("note_recipe", "Pasta sauce recipe uses tomatoes, garlic, olive oil, basil, and parmesan."),
                ProbeNote("note_birthday", "Maya's birthday gift idea: noise-canceling headphones."),
                ProbeNote("note_wifi", "Home Wi-Fi router admin page is at 192.168.1.1."),
            )

        val cases =
            listOf(
                ProbeCase("What do I need to prove for LocalMind on the phone?", "note_project"),
                ProbeCase("When is my dentist appointment?", "note_dentist"),
                ProbeCase("What should I buy for Maya?", "note_birthday"),
                ProbeCase("What ingredients go into the pasta sauce?", "note_recipe"),
                ProbeCase("Where is my router admin page?", "note_wifi"),
            )
    }

    private object ProbeMemory {
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
    }

    private companion object {
        const val LOG_TAG = "ModelCoexistenceSmokeTest"
        const val ARG_PHYSICAL_MODEL_PROBE = "physicalModelProbe"
        const val ARG_APP_COMMIT = "modelProbeAppCommit"
        const val MILLIS_PER_SECOND = 1000L
        const val KILOBYTES_PER_MEGABYTE = 1024L
        const val MIN_MATCH_WORD_LENGTH = 5
        const val PROBE_CSV_FIELD_COUNT = 32
    }
}
