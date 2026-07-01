package il.nfm.localmind.presentation

data class QuestionUiState(
    val query: String = "",
    val retrieved: List<RetrievedNoteUi> = emptyList(),
    val result: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)

data class RetrievedNoteUi(
    val id: String,
    val title: String,
    val score: Float,
)
