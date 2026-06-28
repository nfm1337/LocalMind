package il.nfm.localmind.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import il.nfm.localmind.ml.LLMEngine
import il.nfm.localmind.ml.Retriever
import il.nfm.localmind.ml.buildPrompt
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
                llmEngine.initialize()
            }
        }

        fun query(query: String) {
            _uiState.update { it.copy(query = query, result = "", isLoading = true) }
            viewModelScope.launch {
                val notes = retriever.topK(query)
                val prompt = buildPrompt(query, notes.map { it.content })
                _uiState.update { state -> state.copy(retrieved = notes.map { it.title }) }
                llmEngine
                    .askOnce(prompt)
                    .onCompletion { _uiState.update { it.copy(isLoading = false) } }
                    .collect { token -> _uiState.update { it.copy(result = it.result + token) } }
            }
        }
    }
