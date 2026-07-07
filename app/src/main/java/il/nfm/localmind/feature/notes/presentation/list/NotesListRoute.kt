package il.nfm.localmind.feature.notes.presentation.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NotesListRoute(
    onNoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotesListScreen(
        uiState = uiState,
        onNoteClick = onNoteClick,
        modifier = modifier.fillMaxSize(),
    )
}
