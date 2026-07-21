package il.nfm.localmind.feature.notes.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import il.nfm.localmind.R
import il.nfm.localmind.core.ext.toUpdatedAtLabel
import il.nfm.localmind.core.model.UserNote
import il.nfm.localmind.core.presentation.UiText
import il.nfm.localmind.core.presentation.stringResourceUiText
import il.nfm.localmind.feature.notes.domain.NotesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class NoteDetailsViewModel
    @Inject
    constructor(
        private val notesRepository: NotesRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(NoteDetailsUiState())
        val uiState: StateFlow<NoteDetailsUiState> = _uiState.asStateFlow()

        private val _events = Channel<NoteDetailsEvent>()
        val events = _events.receiveAsFlow()

        private var note: UserNote? = null

        private val saveMutex = Mutex()

        private var loadJob: Job? = null
        private var debounceJob: Job? = null

        private var editRevision = 0L
        private var savedRevision = 0L

        fun load(noteId: String?) {
            if (loadJob != null) return

            if (noteId == null) {
                _uiState.update { state ->
                    state.copy(loadState = NoteLoadState.Ready)
                }
                return
            }

            loadJob =
                viewModelScope.launch {
                    val loadedNote = notesRepository.getNoteById(noteId).first()

                    if (loadedNote == null) {
                        _uiState.update { state ->
                            state.copy(loadState = NoteLoadState.NotFound)
                        }
                        return@launch
                    }

                    note = loadedNote
                    _uiState.value = loadedNote.toUiState()
                }
        }

        fun onTitleChange(title: String) {
            updateDraft(title = title)
        }

        fun onContentChange(content: String) {
            updateDraft(content = content)
        }

        private fun updateDraft(
            title: String? = null,
            content: String? = null,
        ) {
            if (_uiState.value.loadState != NoteLoadState.Ready) return

            _uiState.update { state ->
                state.copy(
                    title = title ?: state.title,
                    content = content ?: state.content,
                    isSaving = true,
                )
            }

            editRevision++
            scheduleSave()
        }

        private fun scheduleSave() {
            debounceJob?.cancel()

            debounceJob =
                viewModelScope.launch {
                    delay(AUTO_SAVE_DELAY_MS.milliseconds)
                    saveLatestVersion()
                }
        }

        suspend fun flush(): Boolean {
            debounceJob?.cancelAndJoin()

            val state = uiState.value

            if (state.title.isBlank() && state.content.isBlank()) {
                _uiState.update { state ->
                    state.copy(isSaving = false)
                }
                return true
            }

            return saveLatestVersion()
        }

        @Suppress("TooGenericExceptionCaught")
        private suspend fun saveLatestVersion(): Boolean =
            saveMutex.withLock {
                val stateSnapshot = _uiState.value
                val revisionSnapshot = editRevision

                if (revisionSnapshot <= savedRevision) {
                    return@withLock true
                }

                if (stateSnapshot.title.isBlank() && stateSnapshot.content.isBlank()) {
                    _uiState.update { state ->
                        state.copy(isSaving = false)
                    }
                    return@withLock true
                }

                try {
                    val existingNote = note

                    val savedNote =
                        if (existingNote == null) {
                            notesRepository.createNote(
                                title = stateSnapshot.title,
                                content = stateSnapshot.content,
                            )
                        } else {
                            existingNote
                                .copy(
                                    title = stateSnapshot.title,
                                    content = stateSnapshot.content,
                                    updatedAt = Clock.System.now(),
                                ).also { updatedNote ->
                                    notesRepository.upsertNote(updatedNote)
                                }
                        }

                    note = savedNote
                    savedRevision = revisionSnapshot

                    _uiState.update { currentState ->
                        currentState.copy(
                            id = savedNote.id,
                            updatedAtLabel = savedNote.updatedAt.toUpdatedAtLabel(),
                            isSaving = editRevision > savedRevision,
                        )
                    }

                    true
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Timber.e(error, "Failed to save note")
                    val message = stringResourceUiText(R.string.error_failed_to_save_note)
                    _events.send(NoteDetailsEvent.ShowSnackbar(message))

                    _uiState.update { state ->
                        state.copy(isSaving = false)
                    }

                    false
                }
            }

        private fun UserNote.toUiState() =
            NoteDetailsUiState(
                id = id,
                title = title,
                content = content,
                updatedAtLabel = updatedAt.toUpdatedAtLabel(),
                loadState = NoteLoadState.Ready,
            )

        private companion object {
            const val AUTO_SAVE_DELAY_MS = 500L
        }
    }

sealed interface NoteDetailsEvent {
    data class ShowSnackbar(
        val message: UiText,
    ) : NoteDetailsEvent
}
