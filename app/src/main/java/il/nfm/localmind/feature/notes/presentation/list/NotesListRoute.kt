package il.nfm.localmind.feature.notes.presentation.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import il.nfm.localmind.core.presentation.UiText

@Composable
fun NotesListRoute(
    onNoteClick: (String) -> Unit,
    onNewNoteClick: () -> Unit,
    onSnackBarMessage: (UiText) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnSnackBarMessage by rememberUpdatedState(onSnackBarMessage)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NotesListEvent.ShowSnackbar -> currentOnSnackBarMessage(event.message)
            }
        }
    }

    NotesListScreen(
        uiState = uiState,
        onNoteClick = onNoteClick,
        onNewNoteClick = onNewNoteClick,
        modifier = modifier,
    )
}
