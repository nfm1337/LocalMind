package il.nfm.localmind.feature.notes.presentation.list

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import il.nfm.localmind.feature.notes.domain.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    }
