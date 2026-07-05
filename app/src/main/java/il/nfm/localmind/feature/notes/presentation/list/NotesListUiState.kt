package il.nfm.localmind.feature.notes.presentation.list

import androidx.compose.runtime.Immutable
import il.nfm.localmind.core.presentation.UiText

data class NotesListUiState(
    val notes: List<NoteUi> = emptyList(),
    val indexingState: IndexingState = IndexingState.Indexed,
    val searchQuery: String = "",
    val error: UiText? = null,
) {
    val notesCount: Int
        get() = notes.size
}

@Immutable
sealed interface IndexingState {
    data object Indexed : IndexingState

    data class Indexing(
        val indexingCount: Int,
    ) : IndexingState
}
