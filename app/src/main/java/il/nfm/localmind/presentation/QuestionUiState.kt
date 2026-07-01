package il.nfm.localmind.presentation

data class QuestionUiState(
    val query: String = "",
    val retrieved: List<String> = emptyList(),
    val result: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)
