package il.nfm.localmind.ml

import il.nfm.localmind.data.model.Note
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.TimeSource

class RetrieverImpl(
    private val embedder: Embedder,
) : Retriever {
    private val _state = MutableStateFlow<Retriever.State>(Retriever.State.Idle)
    val state = _state.asStateFlow()

    @Volatile
    private var index: List<EmbeddedNote> = emptyList()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun build(notes: List<Note>) {
        _state.value = Retriever.State.Indexing
        try {
            embedder.initialize()
            index = notes.map { EmbeddedNote(it, embedder.embedPassage(it.content)) }
            _state.value = Retriever.State.Ready
        } catch (e: CancellationException) {
            _state.value = Retriever.State.Idle
            throw e
        } catch (e: Exception) {
            _state.value = Retriever.State.Error(e)
            throw e
        }
    }

    override suspend fun topK(
        query: String,
        k: Int,
    ): Retriever.Response {
        require(k > 0) { "k must be positive, got $k" }
        check(_state.value == Retriever.State.Ready) { "Retriever index is not ready: ${_state.value}" }
        val beforeQueryEmbedding = TimeSource.Monotonic.markNow()
        val queryVector = embedder.embedQuery(query)
        val queryEmbeddingMs = beforeQueryEmbedding.elapsedNow().inWholeMilliseconds

        val beforeRetrieval = TimeSource.Monotonic.markNow()
        val results =
            index
                .map { embedded ->
                    Retriever.RetrievedNote(
                        note = embedded.note,
                        score = dot(queryVector, embedded.vector),
                    )
                }.sortedByDescending { it.score }
                .take(k)
        val retrievalMs = beforeRetrieval.elapsedNow().inWholeMilliseconds

        return Retriever.Response(
            results = results,
            metrics =
                Retriever.Metrics(
                    queryEmbeddingMs = queryEmbeddingMs,
                    retrievalMs = retrievalMs,
                ),
        )
    }

    private class EmbeddedNote(
        val note: Note,
        val vector: FloatArray,
    )
}
