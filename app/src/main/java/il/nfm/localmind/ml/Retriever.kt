package il.nfm.localmind.ml

import il.nfm.localmind.data.model.Note
import il.nfm.localmind.data.model.RetrievedNote

interface Retriever {
    suspend fun build(notes: List<Note>)

    suspend fun topK(
        query: String,
        k: Int = 5,
    ): List<RetrievedNote>

    sealed interface State {
        data object Idle : State

        data object Indexing : State

        data object Ready : State

        data class Error(
            val cause: Throwable,
        ) : State
    }
}
