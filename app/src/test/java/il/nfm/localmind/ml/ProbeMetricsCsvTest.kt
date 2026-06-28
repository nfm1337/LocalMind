package il.nfm.localmind.ml

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ProbeMetricsCsvTest {
    @Test
    fun metricsHeaderMatchesResultsCsv() {
        val csvHeader = resultsCsv.readLines().first().split(",")

        assertEquals(csvHeader, ProbeMetricsCsv.header)
    }

    @Test
    fun metricsCsvDefinesEveryRequiredField() {
        assertEquals(32, ProbeMetricsCsv.header.size)
        assertEquals(
            listOf(
                "llm_load_ms",
                "embedder_load_ms",
                "tokenizer_load_ms",
                "query_embedding_ms",
                "passage_embedding_10_notes_ms",
                "retrieval_ms",
                "ttft_ms",
                "tokens_per_sec",
                "pss_both_loaded_mb",
                "rss_both_loaded_mb",
                "app_killed",
                "app_froze",
                "oom",
            ),
            ProbeMetricsCsv.header.filter { it in requiredMetricFields },
        )
    }

    private val resultsCsv: File
        get() {
            val candidates =
                listOf(
                    File("docs/model-coexistence-results.csv"),
                    File("../docs/model-coexistence-results.csv"),
                )
            return candidates.first { it.isFile }
        }

    private companion object {
        val requiredMetricFields =
            setOf(
                "llm_load_ms",
                "embedder_load_ms",
                "tokenizer_load_ms",
                "query_embedding_ms",
                "passage_embedding_10_notes_ms",
                "retrieval_ms",
                "ttft_ms",
                "tokens_per_sec",
                "pss_both_loaded_mb",
                "rss_both_loaded_mb",
                "app_killed",
                "app_froze",
                "oom",
            )
    }
}
