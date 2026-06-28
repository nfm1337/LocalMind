package il.nfm.localmind.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import il.nfm.localmind.ml.ModelCoexistenceProbe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel
    @Inject
    constructor(
        private val probe: ModelCoexistenceProbe,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(QuestionUiState())
        val uiState = _uiState.asStateFlow()

        fun query(query: String) {
            _uiState.update {
                it.copy(
                    query = query,
                    retrieved = emptyList(),
                    result = "",
                    metrics = "",
                    isLoading = true,
                )
            }
            viewModelScope.launch {
                val result =
                    probe.run(query) { token ->
                        _uiState.update { it.copy(result = it.result + token) }
                    }
                _uiState.update {
                    it.copy(
                        retrieved = listOf(result.retrievedNote.id),
                        result = result.answer,
                        metrics = result.metrics.summary(),
                        isLoading = false,
                    )
                }
            }
        }
    }
