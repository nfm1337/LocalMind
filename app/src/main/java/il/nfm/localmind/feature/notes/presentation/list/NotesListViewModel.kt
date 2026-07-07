package il.nfm.localmind.feature.notes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import il.nfm.localmind.core.presentation.UiText
import il.nfm.localmind.feature.notes.domain.NotesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel
    @Inject
    constructor(
        initialUiState: NotesListUiState,
        private val notesRepository: NotesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(initialUiState)
        val uiState: StateFlow<NotesListUiState> = _uiState.asStateFlow()
        private val _events = Channel<NotesListEvent>()
        val events = _events.receiveAsFlow()

        init {
            viewModelScope.launch {
                notesRepository.observeNotes().collect { notes ->
                    _uiState.update { state ->
                        state.copy(
                            notes = notes.map { it.toUi() },
                        )
                    }
                }
            }
        }
    }

sealed interface NotesListEvent {
    data class ShowSnackbar(
        val message: UiText,
    ) : NotesListEvent
}
