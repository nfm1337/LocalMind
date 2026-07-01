package il.nfm.localmind.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import il.nfm.localmind.ml.LLMEngine
import il.nfm.localmind.ml.Retriever
import il.nfm.localmind.ml.buildPrompt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel
    @Inject
    constructor(
        private val llmEngine: LLMEngine,
        private val retriever: Retriever,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(QuestionUiState())
        val uiState = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                initializeLlm()
            }
        }

        @Suppress("TooGenericExceptionCaught")
        fun query(query: String) {
            _uiState.update {
                it.copy(
                    query = query,
                    retrieved = emptyList(),
                    result = "",
                    errorMessage = null,
                    isLoading = true,
                )
            }
            viewModelScope.launch {
                try {
                    val retrieval = retriever.topK(query)

                    Log.d(RETRIEVAL_METRICS_TAG, "queryEmbeddingMs: ${retrieval.metrics.queryEmbeddingMs}")
                    Log.d(RETRIEVAL_METRICS_TAG, "retrievalMs: ${retrieval.metrics.retrievalMs}")

                    val notes = retrieval.results

                    _uiState.update { state ->
                        state.copy(
                            retrieved =
                                notes.map {
                                    RetrievedNoteUi(it.note.id, it.note.title, it.score)
                                },
                        )
                    }

                    val prompt = buildPrompt(query, notes.map { it.note.content })
                    llmEngine
                        .askOnce(prompt)
                        .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                        .collect { token -> _uiState.update { it.copy(result = it.result + token) } }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to answer query", e)
                    _uiState.update {
                        it.copy(
                            errorMessage = e.userMessage(),
                            isLoading = false,
                        )
                    }
                }
            }
        }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun initializeLlm() {
            try {
                llmEngine.initialize()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize LLM", e)
                _uiState.update { it.copy(errorMessage = e.userMessage()) }
            }
        }

        private fun Throwable.userMessage(): String =
            message ?: "Model initialization failed. Check that models were pushed to the app."

        private companion object {
            const val TAG = "QuestionViewModel"
            const val RETRIEVAL_METRICS_TAG = "RetrievalMetrics"
        }
    }
