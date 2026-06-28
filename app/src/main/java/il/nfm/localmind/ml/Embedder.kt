package il.nfm.localmind.ml

import kotlinx.coroutines.flow.Flow

interface Embedder {
    val state: Flow<State>

    suspend fun initialize(): EmbedderLoadMetrics

    suspend fun embedQuery(text: String): FloatArray

    suspend fun embedPassage(text: String): FloatArray

    suspend fun close()

    sealed interface State {
        data object Idle : State

        data object Initializing : State

        data object Ready : State

        data class Error(
            val cause: Throwable,
        ) : State
    }
}

data class EmbedderLoadMetrics(
    val embedderLoadMs: Long,
    val tokenizerLoadMs: Long,
)
