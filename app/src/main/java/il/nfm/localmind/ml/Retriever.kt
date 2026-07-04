package il.nfm.localmind.ml

import il.nfm.localmind.data.model.Note
import kotlinx.coroutines.flow.StateFlow

interface Retriever {
    val state: StateFlow<State>

    suspend fun build(notes: List<Note>)

    suspend fun topK(
        query: String,
        k: Int = 5,
    ): Response

    data class Response(
        val results: List<RetrievedNote>,
        val metrics: Metrics,
    )

    data class RetrievedNote(
        val note: Note,
        val score: Float,
    )

    data class Metrics(
        val queryEmbeddingMs: Long,
        val retrievalMs: Long,
    )

    sealed interface State {
        data object Idle : State

        data object Indexing : State

        data object Ready : State

        data class Error(
            val cause: Throwable,
        ) : State
    }
}
