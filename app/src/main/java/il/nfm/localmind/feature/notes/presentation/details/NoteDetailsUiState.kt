package il.nfm.localmind.feature.notes.presentation.details

data class NoteDetailsUiState(
    val id: String? = null,
    val title: String = "",
    val content: String = "",
    val updatedAtLabel: String? = null,
    val loadState: NoteLoadState = NoteLoadState.Initial,
    val isSaving: Boolean = false,
)

enum class NoteLoadState {
    Initial,
    Ready,
    NotFound,
}
