package il.nfm.localmind.ml

import il.nfm.localmind.data.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RetrieverTest {
    @Test
    fun `topK rejects negative k`() =
        runTest {
            val retriever = RetrieverImpl(FakeEmbedder(emptyMap(), emptyMap()))
            assertFailsWith<IllegalArgumentException> {
                retriever.topK("query", -1)
            }
        }

    @Test
    fun `topK rejects zero k`() =
        runTest {
            val retriever = RetrieverImpl(FakeEmbedder(emptyMap(), emptyMap()))
            assertFailsWith<IllegalArgumentException> {
                retriever.topK("query", 0)
            }
        }

    @Test
    fun `topK fails when index is not ready`() =
        runTest {
            val retriever = RetrieverImpl(FakeEmbedder(emptyMap(), emptyMap()))
            assertFailsWith<IllegalStateException> {
                retriever.topK("query")
            }
        }

    @Test
    fun `topK returns notes sorted by descending score`() =
        runTest {
            val retriever = builtRetrieverWithTwoNotes()

            val result = retriever.topK("query")

            assertEquals(
                expected = listOf("b", "a"),
                actual = result.map { it.note.id },
            )
        }

    @Test
    fun `topK limits results to requested k`() =
        runTest {
            val retriever = builtRetrieverWithTwoNotes()

            val result = retriever.topK("query", k = 1)

            assertEquals(
                expected = listOf("b"),
                actual = result.map { it.note.id },
            )
        }

    @Test
    fun `topK returns empty list when index is empty`() =
        runTest {
            val embedder =
                FakeEmbedder(
                    queryVectors = mapOf("query" to floatArrayOf(1f, 0f)),
                    passageVectors = mapOf(),
                )

            val retriever = RetrieverImpl(embedder)
            retriever.build(emptyList())

            val result = retriever.topK("query", k = 1)

            assertEquals(
                expected = emptyList(),
                actual = result,
            )
        }

    @Test
    fun `topK returns all available results when index size is smaller than k`() =
        runTest {
            val retriever = builtRetrieverWithTwoNotes()

            val k = 5
            val result = retriever.topK("query", k = k)

            assertEquals(
                expected = listOf("b", "a"),
                actual = result.map { it.note.id },
            )
        }

    private suspend fun builtRetrieverWithTwoNotes(): Retriever {
        val notes =
            listOf(
                Note(id = "a", title = "A", content = "alpha"),
                Note(id = "b", title = "B", content = "beta"),
            )

        val embedder =
            FakeEmbedder(
                queryVectors = mapOf("query" to floatArrayOf(1f, 0f)),
                passageVectors = mapOf("alpha" to floatArrayOf(0.2f, 0f), "beta" to floatArrayOf(0.9f, 0f)),
            )

        return RetrieverImpl(embedder).also { it.build(notes) }
    }
}

private class FakeEmbedder(
    private val queryVectors: Map<String, FloatArray>,
    private val passageVectors: Map<String, FloatArray>,
) : Embedder {
    override val state: Flow<Embedder.State> = flowOf(Embedder.State.Ready)

    override suspend fun initialize(): EmbedderLoadMetrics =
        EmbedderLoadMetrics(
            embedderLoadMs = 0,
            tokenizerLoadMs = 0,
        )

    override suspend fun embedQuery(text: String): FloatArray = queryVectors.getValue(text)

    override suspend fun embedPassage(text: String): FloatArray = passageVectors.getValue(text)

    override suspend fun close() = Unit
}
