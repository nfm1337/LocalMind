package il.nfm.localmind.ml

import kotlinx.coroutines.flow.Flow

interface LLMEngine {
    val state: Flow<State>

    suspend fun initialize()

    suspend fun close()

    fun askOnce(prompt: String): Flow<String>

    sealed interface State {
        data object Idle : State

        data object Initializing : State

        data object Ready : State

        data class Error(
            val cause: Throwable,
        ) : State
    }
}
