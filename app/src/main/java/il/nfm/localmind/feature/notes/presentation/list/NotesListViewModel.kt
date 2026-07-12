package il.nfm.localmind.feature.notes.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import il.nfm.localmind.R
import il.nfm.localmind.core.presentation.UiText
import il.nfm.localmind.core.presentation.pluralResourceUiText
import il.nfm.localmind.core.presentation.stringResourceUiText
import il.nfm.localmind.feature.notes.domain.NotesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

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

        fun toggleNoteSelection(noteId: String) {
            _uiState.update { state ->
                if (noteId in state.selectedNoteIds) {
                    state.copy(selectedNoteIds = state.selectedNoteIds - noteId)
                } else {
                    state.copy(selectedNoteIds = state.selectedNoteIds + noteId)
                }
            }
        }

        fun clearSelection() {
            _uiState.update { state ->
                state.copy(selectedNoteIds = emptySet())
            }
        }

        @Suppress("TooGenericExceptionCaught")
        fun deleteSelection() {
            viewModelScope.launch {
                val noteIdsToDelete = uiState.value.selectedNoteIds
                if (noteIdsToDelete.isEmpty()) return@launch

                try {
                    notesRepository.deleteNotesByIds(noteIdsToDelete)

                    _uiState.update { state ->
                        state.copy(selectedNoteIds = state.selectedNoteIds - noteIdsToDelete)
                    }

                    _events.send(
                        NotesListEvent.ShowSnackbar(
                            pluralResourceUiText(
                                R.plurals.notes_deleted_count,
                                noteIdsToDelete.size,
                                noteIdsToDelete.size,
                            ),
                        ),
                    )
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Timber.e(error, "Error deleting notes")
                    _events.send(
                        NotesListEvent.ShowSnackbar(
                            stringResourceUiText(R.string.notes_deletion_error),
                        ),
                    )
                }
            }
        }
    }

sealed interface NotesListEvent {
    data class ShowSnackbar(
        val message: UiText,
    ) : NotesListEvent
}
