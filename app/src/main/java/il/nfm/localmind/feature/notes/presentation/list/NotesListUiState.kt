package il.nfm.localmind.feature.notes.presentation.list

import androidx.compose.runtime.Immutable

data class NotesListUiState(
    val notes: List<NoteUi> = emptyList(),
    val indexingState: IndexingState = IndexingState.Indexed,
    val searchQuery: String = "",
    val selectedNoteIds: Set<String> = emptySet(),
) {
    val notesCount: Int
        get() = notes.size
    val selectedCount: Int get() = selectedNoteIds.size
    val isSelectionMode: Boolean get() = selectedNoteIds.isNotEmpty()
}

@Immutable
sealed interface IndexingState {
    data object Indexed : IndexingState

    data class Indexing(
        val indexingCount: Int,
    ) : IndexingState
}
